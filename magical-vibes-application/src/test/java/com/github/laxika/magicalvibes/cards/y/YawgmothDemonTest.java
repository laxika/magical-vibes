package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YawgmothDemon.class, Ornithopter.class, MycosynthLattice.class, Boomerang.class, LoxodonWarhammer.class})
class YawgmothDemonTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the sacrifice taps the Demon and deals 2 damage to its controller")
    void declineTapsAndDealsDamage() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 2);
        // Artifact was not sacrificed
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("Accepting with a single artifact sacrifices it with no penalty")
    void acceptSacrificesArtifactNoPenalty() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isFalse();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Accepting with multiple artifacts prompts a choice; only the chosen one is sacrificed")
    void acceptWithMultipleArtifactsPromptsChoice() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        UUID chosenArtifact = harness.getPermanentId(player1, "Ornithopter");

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, chosenArtifact);

        long artifactsLeft = countPermanents(player1, "Ornithopter");
        assertThat(artifactsLeft).isEqualTo(1);
        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isFalse();
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("With no artifacts, the penalty applies immediately without a prompt")
    void noArtifactsAppliesPenalty() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → penalty (no artifact to sacrifice)

        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("Can sacrifice itself when it has become an artifact")
    void canSacrificeItselfWhenItIsAnArtifact() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new MycosynthLattice());
        UUID demonId = harness.getPermanentId(player1, "Yawgmoth Demon");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, demonId);

        harness.assertInGraveyard(player1, "Yawgmoth Demon");
        harness.assertOnBattlefield(player1, "Mycosynth Lattice");
        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("Cannot use an artifact controlled by an opponent to pay the upkeep cost")
    void opponentArtifactDoesNotPay() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player2, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 2);
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        harness.addToBattlefield(player1, new Ornithopter());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isFalse();
        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Ornithopter");
    }

    @Test
    @DisplayName("An already tapped Demon still deals its upkeep damage")
    void alreadyTappedDemonStillDealsDamage() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        findPermanent(player1, "Yawgmoth Demon").tap();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("The upkeep penalty still deals damage after the Demon leaves the battlefield")
    void removedDemonStillDealsDamage() {
        harness.addToBattlefield(player1, new YawgmothDemon());
        UUID demonId = harness.getPermanentId(player1, "Yawgmoth Demon");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, demonId);
        harness.assertInHand(player1, "Yawgmoth Demon");
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 2);
    }

    @Test
    @DisplayName("Lifelink gains life from the Demon's upkeep damage")
    void upkeepDamageAppliesLifelink() {
        harness.addToBattlefield(player1, new LoxodonWarhammer());
        harness.addToBattlefield(player1, new YawgmothDemon());
        UUID demonId = harness.getPermanentId(player1, "Yawgmoth Demon");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, demonId);
        harness.passBothPriorities();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Yawgmoth Demon").isTapped()).isTrue();
        harness.assertLife(player1, lifeBefore);
    }
}
