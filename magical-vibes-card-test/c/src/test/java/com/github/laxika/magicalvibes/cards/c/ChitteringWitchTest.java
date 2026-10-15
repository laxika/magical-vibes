package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitteringWitch.class, GnarledMass.class, GrizzlyBears.class, Swamp.class})
class ChitteringWitchTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one Rat for the sole opponent")
    void etbCreatesRatForEachOpponent() {
        castWitch();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        Permanent rat = findPermanent(player1, "Rat");
        assertThat(rat.getEffectivePower()).isEqualTo(1);
        assertThat(rat.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB creates one Rat for each opponent in a multiplayer game")
    void etbCountsAllOpponents() {
        Player player3 = addThirdPlayer();
        castWitch();

        assertThat(findPermanents(player1, "Rat")).hasSize(2);
        assertThat(gd.playerBattlefields.get(player3.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a creature gives the target -2/-2 until end of turn")
    void sacrificeCreatureGivesTargetMinusTwoMinusTwo() {
        Permanent witch = addCreatureReady(player1, new ChitteringWitch());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GnarledMass());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(witch),
                null, target.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent witch = addCreatureReady(player1, new ChitteringWitch());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(witch), null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    @DisplayName("The Witch can sacrifice itself while summoning sick and its ability still resolves")
    void canSacrificeItselfImmediately() {
        castWitch();
        Permanent witch = findPermanent(player1, "Chittering Witch");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChitteringWitch());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(witch),
                null, target.getId());
        harness.handlePermanentChosen(player1, witch.getId());

        harness.assertInGraveyard(player1, "Chittering Witch");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Chittering Witch");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(findPermanents(player1, "Rat")).hasSize(1);
    }

    @Test
    @DisplayName("A Rat token can pay the sacrifice cost to target your own Witch")
    void canSacrificeRatToTargetOwnCreature() {
        castWitch();
        Permanent witch = findPermanent(player1, "Chittering Witch");
        Permanent rat = findPermanent(player1, "Rat");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(witch),
                null, witch.getId());
        harness.handlePermanentChosen(player1, rat.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rat).contains(witch);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chittering Witch");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void castWitch() {
        harness.castFromHand(player1, new ChitteringWitch(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Player addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        Player player3 = new Player(player3Id, "Charlie");
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(player3Id, "Charlie");
        gd.playerDecks.put(player3Id, new ArrayList<>());
        gd.playerHands.put(player3Id, new ArrayList<>());
        gd.playerBattlefields.put(player3Id, new ArrayList<>());
        gd.playerGraveyards.put(player3Id, new ArrayList<>());
        gd.playerCommandZones.put(player3Id, new ArrayList<>());
        gd.playerManaPools.put(player3Id, new ManaPool());
        gd.playerLifeTotals.put(player3Id, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), player3Id, "Charlie");
        return player3;
    }
}
