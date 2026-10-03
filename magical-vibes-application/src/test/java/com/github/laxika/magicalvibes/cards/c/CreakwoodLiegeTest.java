package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreakwoodLiege.class, BlackKnight.class, GrizzlyBears.class, HillGiant.class})
class CreakwoodLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other black creatures you control")
    void buffsOtherBlack() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent black = findPermanent(player1, "Black Knight");

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, black)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, black)).isEqualTo(3);
    }

    @Test
    @DisplayName("Buffs other green creatures you control")
    void buffsOtherGreen() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent green = findPermanent(player1, "Grizzly Bears");

        // 2/2 base + 1/1 = 3/3
        assertThat(gqs.getEffectivePower(gd, green)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, green)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself or off-color creatures")
    void doesNotBuffItselfOrOffColor() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent red = findPermanent(player1, "Hill Giant");

        // Red creature is neither black nor green.
        assertThat(gqs.getEffectivePower(gd, red)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, red)).isEqualTo(3);
        Permanent liege = findPermanent(player1, "Creakwood Liege");
        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(2);
    }

    @Test
    @DisplayName("Upkeep trigger may create a 1/1 black and green Worm token")
    void upkeepCreatesWormToken() {
        harness.addToBattlefield(player1, new CreakwoodLiege());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countWormTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger creates no token")
    void upkeepDeclinedCreatesNoToken() {
        harness.addToBattlefield(player1, new CreakwoodLiege());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve MayEffect from stack → may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countWormTokens(player1)).isZero();
    }

    @Test
    @DisplayName("The created Worm token is black and green so both anthems apply")
    void wormTokenIsBuffedByBothAnthems() {
        harness.addToBattlefield(player1, new CreakwoodLiege());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent worm = findPermanent(player1, "Worm");
        // 1/1 base + 1/1 (black anthem) + 1/1 (green anthem) = 3/3
        assertThat(gqs.getEffectivePower(gd, worm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, worm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Anthems do not boost opponents' black or green creatures")
    void doesNotBoostOpposingCreatures() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player2, new BlackKnight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        for (Permanent creature : gd.playerBattlefields.get(player2.getId())) {
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Two Lieges each receive both boosts from the other Liege")
    void twoLiegesBoostEachOther() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        harness.addToBattlefield(player1, new CreakwoodLiege());

        for (Permanent liege : findPermanents(player1, "Creakwood Liege")) {
            assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("No token ability triggers during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new CreakwoodLiege());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countWormTokens(player1)).isZero();
        assertThat(countWormTokens(player2)).isZero();
    }

    @Test
    @DisplayName("The upkeep ability still creates an unboosted token after the Liege leaves")
    void triggerResolvesAfterLiegeLeaves() {
        harness.addToBattlefield(player1, new CreakwoodLiege());
        Permanent liege = findPermanent(player1, "Creakwood Liege");
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(liege);
        gd.playerGraveyards.get(player1.getId()).add(liege.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countWormTokens(player1)).isEqualTo(1);
        Permanent worm = findPermanent(player1, "Worm");
        assertThat(gqs.getEffectivePower(gd, worm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, worm)).isEqualTo(1);
    }

    private int countWormTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Worm"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.WORM))
                .count();
    }
}
