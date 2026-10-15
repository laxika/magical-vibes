package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FaithfulSquire;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.h.HeartOfLight;
import com.github.laxika.magicalvibes.cards.s.Shuko;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmptyShrineKannushi.class, FaithfulSquire.class, Frostling.class, GoblinCohort.class, HeartOfLight.class, Shuko.class})
class EmptyShrineKannushiTest extends BaseCardTest {

    private void attackWithKannushi() {
        addCreatureReady(player1, new EmptyShrineKannushi());
        declareAttackersAndPrepareBlockers(List.of(0));
    }

    @Test
    @DisplayName("A white creature cannot block Empty-Shrine Kannushi, which is itself white")
    void whiteCreatureCannotBlock() {
        attackWithKannushi();

        addCreatureReady(player2, new FaithfulSquire());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A red creature can block while its controller controls no red permanent")
    void redCreatureCanBlockWithoutRedPermanent() {
        attackWithKannushi();

        Permanent blocker = addCreatureReady(player2, new GoblinCohort());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gaining a red permanent extends the protection to red creatures")
    void redCreatureCannotBlockOnceControllerHasRedPermanent() {
        attackWithKannushi();

        addCreatureReady(player1, new Frostling());

        addCreatureReady(player2, new GoblinCohort());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents a red creature ability from targeting it")
    void redAbilityCannotTargetWhenControllerHasRedPermanent() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player1, new Frostling());
        addCreatureReady(player2, new Frostling());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, kannushi.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("An opponent's red permanent does not grant protection from red")
    void opponentRedPermanentDoesNotGrantProtection() {
        attackWithKannushi();

        Permanent blocker = addCreatureReady(player2, new GoblinCohort());
        addCreatureReady(player2, new GoblinCohort());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can die simultaneously with another creature without reevaluating protection off the battlefield")
    void canDieSimultaneouslyWithAnotherCreature() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new EmptyShrineKannushi());
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        kannushi.setMarkedDamage(1);
        frostling.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Empty-Shrine Kannushi", "Frostling");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from a red creature")
    void preventsCombatDamageFromRedCreature() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player1, new Frostling());
        addCreatureReady(player2, new Frostling());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(kannushi.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Empty-Shrine Kannushi", "Frostling");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Frostling");
    }

    @Test
    @DisplayName("Can die in combat while damage triggers are collected")
    void canDieInCombatWhileDamageTriggersAreCollected() {
        addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player2, new GoblinCohort());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Empty-Shrine Kannushi");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getMarkedDamage()).isEqualTo(1));
    }

    @Test
    @DisplayName("A red ability resolves normally when its target has no protection from red")
    void redAbilityCanKillWithoutRedPermanent() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player2, new Frostling());

        harness.activateAbility(player2, 0, null, kannushi.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Empty-Shrine Kannushi");
        harness.assertInGraveyard(player1, "Empty-Shrine Kannushi");
    }

    @Test
    @DisplayName("A red ability loses its legal target if a red permanent enters before resolution")
    void gainingRedPermanentInvalidatesPendingTarget() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player2, new Frostling());
        harness.activateAbility(player2, 0, null, kannushi.getId());

        addCreatureReady(player1, new Frostling());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Empty-Shrine Kannushi");
        assertThat(kannushi.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Sacrificing the last red permanent immediately removes protection from red")
    void sacrificingLastRedPermanentRemovesProtection() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player1, new Frostling());
        Permanent cohort = addCreatureReady(player2, new GoblinCohort());
        addCreatureReady(player2, new Frostling());

        harness.activateAbility(player1, 1, null, cohort.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, 1, null, kannushi.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Empty-Shrine Kannushi");
        harness.assertNotOnBattlefield(player1, "Empty-Shrine Kannushi");
    }

    @Test
    @DisplayName("Protection from its own color prevents a white Aura from targeting Kannushi")
    void whiteAuraCannotTarget() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        harness.setHand(player1, List.of(new HeartOfLight()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, kannushi.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A white Aura attached without targeting is removed by protection")
    void attachedWhiteAuraIsPutIntoGraveyard() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HeartOfLight());
        aura.setAttachedTo(kannushi.getId());

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Empty-Shrine Kannushi");
        harness.assertNotOnBattlefield(player2, "Heart of Light");
        harness.assertInGraveyard(player2, "Heart of Light");
    }

    @Test
    @DisplayName("Controlling colorless Equipment does not give protection from colorless")
    void colorlessEquipmentCanTargetAndRemainAttached() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        Permanent shuko = harness.addToBattlefieldAndReturn(player1, new Shuko());

        harness.activateAbility(player1, 1, null, kannushi.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        assertThat(shuko.getAttachedTo()).isEqualTo(kannushi.getId());
        harness.assertOnBattlefield(player1, "Shuko");
    }
}
