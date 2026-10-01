package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatDesertHellion.class, GrizzlyBears.class})
class GreatDesertHellionTest extends BaseCardTest {

    @Test
    void startsAtIntensityOneAndIntensifiesWhenDiscardingAtUpkeep() {
        GreatDesertHellion hellionCard = new GreatDesertHellion();
        harness.setHand(player1, List.of(hellionCard, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hellion = findHellion();

        assertThat(gd.getCardIntensity(hellion.getCard().getId())).isEqualTo(1);

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardIntensity(hellion.getCard().getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellion);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningToDiscardSacrificesGreatDesertHellion() {
        Permanent hellion = harness.addToBattlefieldAndReturn(player1, new GreatDesertHellion());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hellion);
        harness.assertInGraveyard(player1, "Great Desert Hellion");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void leavingMayDiscardHandAndDrawItsIntensity() {
        GreatDesertHellion hellionCard = new GreatDesertHellion();
        Permanent hellion = harness.addToBattlefieldAndReturn(player1, hellionCard);
        gd.intensifyCard(hellionCard, 2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hellion));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardIntensity(hellionCard.getId())).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Great Desert Hellion");
    }

    private void resolveUpkeepTrigger() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }

    private Permanent findHellion() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Great Desert Hellion"))
                .findFirst()
                .orElseThrow();
    }
}
