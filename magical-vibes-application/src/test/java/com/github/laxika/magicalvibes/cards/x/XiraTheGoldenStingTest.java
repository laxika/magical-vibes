package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({XiraTheGoldenSting.class, GrizzlyBears.class, Shock.class})
class XiraTheGoldenStingTest extends BaseCardTest {

    @Test
    @DisplayName("Marks only an eligible creature and rewards its later death")
    void marksEligibleCreatureAndRewardsItsDeath() {
        Permanent xira = addCreatureReady(player1, new XiraTheGoldenSting());
        Permanent alreadyMarked = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        alreadyMarked.setCounterCount(CounterType.EGG, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId())
                .doesNotContain(xira.getId(), alreadyMarked.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.EGG)).isEqualTo(1);

        int handAfterMarking = gd.playerHands.get(player1.getId()).size();
        harness.castInstant(player1, 0, alreadyMarked.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterMarking - 1);
        assertThat(insectTokens(player1)).isEmpty();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handAfterMarking - 1);
        assertThat(insectTokens(player1)).hasSize(1);
        Permanent insect = insectTokens(player1).getFirst();
        assertThat(insect.getCard().getSubtypes()).contains(CardSubtype.INSECT);
        assertThat(gqs.hasKeyword(gd, insect, Keyword.FLYING)).isTrue();
    }

    private List<Permanent> insectTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Insect"))
                .toList();
    }
}
