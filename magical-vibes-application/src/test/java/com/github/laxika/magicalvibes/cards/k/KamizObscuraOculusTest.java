package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamizObscuraOculus.class, FugitiveWizard.class, GrizzlyBears.class, Mountain.class})
class KamizObscuraOculusTest extends BaseCardTest {

    @Test
    void attacksMakeTargetConniveAndGrantDoubleStrikeToLesserAttacker() {
        Permanent kamiz = addCreatureReady(player1, new KamizObscuraOculus());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0, 1, 2));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(kamiz.getId(), wizard.getId(), bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        discardByName("Grizzly Bears");

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.validIds()).containsExactlyInAnyOrder(kamiz.getId(), wizard.getId())
                .doesNotContain(bears.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(wizard.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, bears)).isTrue();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, kamiz, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void doesNotOfferChoiceWhenNoAttackerHasLesserPower() {
        Permanent kamiz = addCreatureReady(player1, new KamizObscuraOculus());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0, 1, 2));
        harness.handlePermanentChosen(player1, wizard.getId());
        harness.passBothPriorities();
        discardByName("Grizzly Bears");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, wizard)).isTrue();
        assertThat(gqs.hasKeyword(gd, kamiz, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
