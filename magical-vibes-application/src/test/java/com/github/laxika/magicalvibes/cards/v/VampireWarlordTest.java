package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampireWarlord.class, CoralMerfolk.class, CanyonMinotaur.class})
class VampireWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants a regeneration shield")
    void sacrificingAnotherCreatureGrantsShield() {
        addCreatureReady(player1, new VampireWarlord());
        harness.addToBattlefield(player1, new CoralMerfolk());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coral Merfolk");
        assertThat(findPermanent(player1, "Vampire Warlord").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate when Vampire Warlord is the only creature")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new VampireWarlord());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vampire Warlord");
    }

    @Test
    @DisplayName("Choosing Vampire Warlord itself as the sacrifice is rejected")
    void choosingItselfIsRejected() {
        Permanent warlord = addCreatureReady(player1, new VampireWarlord());
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, warlord.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vampire Warlord");
        harness.assertOnBattlefield(player1, "Coral Merfolk");
        harness.assertOnBattlefield(player1, "Canyon Minotaur");
    }

    @Test
    @DisplayName("Regeneration shield saves Vampire Warlord from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent warlord = addCreatureReady(player1, new VampireWarlord());
        warlord.setRegenerationShield(1);
        warlord.setBlocking(true);
        warlord.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new CanyonMinotaur());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Vampire Warlord");
        Permanent survivor = findPermanent(player1, "Vampire Warlord");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Vampire Warlord dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent warlord = addCreatureReady(player1, new VampireWarlord());
        warlord.setBlocking(true);
        warlord.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new CanyonMinotaur());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Vampire Warlord");
    }

    @Test
    @DisplayName("Chosen creature is sacrificed when several are available")
    void chosenCreatureIsSacrificed() {
        addCreatureReady(player1, new VampireWarlord());
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new CanyonMinotaur());
        UUID minotaurId = harness.getPermanentId(player1, "Canyon Minotaur");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, minotaurId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canyon Minotaur");
        harness.assertOnBattlefield(player1, "Coral Merfolk");
        assertThat(findPermanent(player1, "Vampire Warlord").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before the regeneration ability resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new VampireWarlord());
        harness.addToBattlefield(player1, new CoralMerfolk());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Coral Merfolk");
        assertThat(warlord.getRegenerationShield()).isZero();
        assertThat(warlord.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(warlord.getRegenerationShield()).isEqualTo(1);
        assertThat(warlord.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Warlord can regenerate without mana")
    void tappedSummoningSickWarlordCanActivate() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new VampireWarlord());
        warlord.setSummoningSick(true);
        warlord.tap();
        harness.addToBattlefield(player1, new CoralMerfolk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coral Merfolk");
        assertThat(warlord.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new VampireWarlord());
        harness.addToBattlefield(player2, new CoralMerfolk());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vampire Warlord");
        harness.assertOnBattlefield(player2, "Coral Merfolk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An activated regeneration shield removes combat damage and the creature from combat")
    void activatedShieldPreventsLethalCombatDamage() {
        Permanent warlord = addCreatureReady(player1, new VampireWarlord());
        harness.addToBattlefield(player1, new CoralMerfolk());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        warlord.setBlocking(true);
        warlord.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new CanyonMinotaur());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Vampire Warlord");
        assertThat(warlord.isTapped()).isTrue();
        assertThat(warlord.getRegenerationShield()).isZero();
        assertThat(warlord.getMarkedDamage()).isZero();
        assertThat(warlord.isBlocking()).isFalse();
        assertThat(warlord.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Another Vampire Warlord can be sacrificed, but the source survives")
    void canSacrificeAnotherWarlord() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new VampireWarlord());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new VampireWarlord());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).doesNotContain(other);
        harness.assertInGraveyard(player1, "Vampire Warlord");
        assertThat(source.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each activation can add another shield by sacrificing another creature")
    void repeatedActivationsAddShields() {
        Permanent warlord = harness.addToBattlefieldAndReturn(player1, new VampireWarlord());
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());
        harness.addToBattlefield(player1, new CanyonMinotaur());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, merfolk.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coral Merfolk");
        harness.assertInGraveyard(player1, "Canyon Minotaur");
        assertThat(warlord.getRegenerationShield()).isEqualTo(2);
    }
}
