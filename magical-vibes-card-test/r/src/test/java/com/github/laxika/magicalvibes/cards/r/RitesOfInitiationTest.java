package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RitesOfInitiation.class, AvenFisher.class, Mountain.class})
class RitesOfInitiationTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures +1/+0 for each randomly discarded card")
    void boostsOwnCreaturesByRandomDiscardCount() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        Permanent opponentCreature = addCreature(player2);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new AvenFisher(), new AvenFisher(), new AvenFisher()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 2);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Choosing zero cards leaves creatures unchanged")
    void canDiscardZeroCards() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new AvenFisher()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new AvenFisher()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost noncreatures you control")
    void doesNotBoostOwnNoncreatures() {
        Permanent creature = addCreature(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new RitesOfInitiation(), new AvenFisher()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, mountain)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, mountain)).isZero();
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new AvenFisher());
    }

    @Test
    @DisplayName("Resolves without a discard choice when your hand is empty")
    void resolvesWithEmptyHand() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new RitesOfInitiation()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rites of Initiation");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can discard the entire remaining hand without boosting toughness")
    void canDiscardEntireHand() {
        Permanent creature = addCreature(player1);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new AvenFisher(), new Mountain()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Aven Fisher");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void doesNotBoostCreaturesEnteringLater() {
        Permanent existing = addCreature(player1);
        harness.setHand(player1, List.of(new RitesOfInitiation(), new AvenFisher()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleXValueChosen(player1, 1);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player1, new AvenFisher());

        assertThat(gqs.getEffectivePower(gd, existing)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(2);
    }
}
