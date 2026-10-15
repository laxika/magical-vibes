package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.c.CylianSunsinger;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.r.ReliquaryTower;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ObeliskOfAlara.class, CylianSunsinger.class, ReliquaryTower.class,
        CanyonMinotaur.class, NicolBolasPlaneswalker.class})
class ObeliskOfAlaraTest extends BaseCardTest {

    @Test
    @DisplayName("White ability gains 5 life")
    void whiteAbilityGainsFiveLife() {
        harness.setLife(player1, 20);
        addReadyObelisk(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Blue ability draws a card then discards a card")
    void blueAbilityLoots() {
        addReadyObelisk(player1);
        harness.setLibrary(player1, List.of(new ReliquaryTower()));
        harness.setHand(player1, List.of(new CylianSunsinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Drew the Reliquary Tower (hand = 2), now must discard one.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Black ability kills a 2/2 creature")
    void blackAbilityKillsCylianSunsinger() {
        addReadyObelisk(player1);
        harness.addToBattlefield(player2, new CylianSunsinger());
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Cylian Sunsinger");
        harness.activateAbility(player1, 0, 2, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cylian Sunsinger");
        harness.assertInGraveyard(player2, "Cylian Sunsinger");
    }

    @Test
    @DisplayName("Black ability cannot target a noncreature permanent")
    void blackAbilityCannotTargetNoncreature() {
        addReadyObelisk(player1);
        harness.addToBattlefield(player2, new ReliquaryTower());
        UUID towerId = harness.getPermanentId(player2, "Reliquary Tower");
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, towerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Red ability deals 3 damage to target player")
    void redAbilityDealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyObelisk(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Red ability can target its own controller — it says target player, not target opponent")
    void redAbilityCanTargetItsOwnController() {
        harness.setLife(player1, 20);
        addReadyObelisk(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 3, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Green ability gives target creature +4/+4")
    void greenAbilityBuffsTargetCreature() {
        addReadyObelisk(player1);
        harness.addToBattlefield(player1, new CylianSunsinger());
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Cylian Sunsinger");
        harness.activateAbility(player1, 0, 4, null, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Cylian Sunsinger");
        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Green buff wears off at cleanup")
    void greenBuffWearsOffAtCleanup() {
        addReadyObelisk(player1);
        harness.addToBattlefield(player1, new CylianSunsinger());
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID targetId = harness.getPermanentId(player1, "Cylian Sunsinger");
        harness.activateAbility(player1, 0, 4, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Cylian Sunsinger");
        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    void redAbilityDamagesPlaneswalker() {
        addReadyObelisk(player1);
        Permanent bolas = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        bolas.setCounterCount(CounterType.LOYALTY, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 3, null, bolas.getId());
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    void redAbilityCannotTargetCreature() {
        addReadyObelisk(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blueAbilityCanDiscardTheDrawnCard() {
        addReadyObelisk(player1);
        harness.setLibrary(player1, List.of(new ReliquaryTower()));
        harness.setHand(player1, List.of(new CylianSunsinger()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Reliquary Tower");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Cylian Sunsinger");
    }

    @Test
    void blackDebuffWearsOffAtCleanup() {
        addReadyObelisk(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CanyonMinotaur());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 2, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(-2);
        assertThat(creature.getToughnessModifier()).isEqualTo(-2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Canyon Minotaur");
    }

    @Test
    void greenAbilityCanBoostOpponentsCreature() {
        addReadyObelisk(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianSunsinger());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 4, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(4);
        assertThat(creature.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    void tapCostPreventsActivatingAnotherMode() {
        addReadyObelisk(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findPermanent(player1, "Obelisk of Alara").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertLife(player1, 25);
    }

    @Test
    void noncreatureArtifactCanActivateOnTurnItEnters() {
        harness.addToBattlefield(player1, new ObeliskOfAlara());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
    }

    @Test
    void activationRequiresTheSpecifiedColor() {
        addReadyObelisk(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Obelisk of Alara").isTapped()).isFalse();
        harness.assertLife(player1, 20);
    }

    private void addReadyObelisk(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ObeliskOfAlara());
        perm.setSummoningSick(false);
    }
}
