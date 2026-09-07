package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.PhyrexianBroodlings;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiftOfCompleation.class, PhyrexianBroodlings.class, GrizzlyBears.class, Shock.class})
class GiftOfCompleationTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an Incubator token with three +1/+1 counters")
    void entersWithIncubatorToken() {
        castGiftOfCompleation();

        assertThat(findPermanent(player1, "Incubator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Surveils when a Phyrexian you control dies")
    void surveilsWhenPhyrexianYouControlDies() {
        harness.addToBattlefield(player1, new GiftOfCompleation());
        Permanent phyrexian = harness.addToBattlefieldAndReturn(player1, new PhyrexianBroodlings());
        Card topCard = new PhyrexianBroodlings();
        harness.setLibrary(player1, List.of(topCard));

        killWithShock(phyrexian);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Does not surveil when a non-Phyrexian you control dies")
    void doesNotSurveilWhenNonPhyrexianDies() {
        harness.addToBattlefield(player1, new GiftOfCompleation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card topCard = new PhyrexianBroodlings();
        harness.setLibrary(player1, List.of(topCard));

        killWithShock(creature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCard);
    }

    private void castGiftOfCompleation() {
        harness.setHand(player1, List.of(new GiftOfCompleation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
