package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvilTwinTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Evil Twin has CopyPermanentOnEnterEffect with additional activated ability")
    void hasCorrectProperties() {
        EvilTwin card = new EvilTwin();

        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(CopyPermanentOnEnterEffect.class);

        CopyPermanentOnEnterEffect effect = (CopyPermanentOnEnterEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.additionalActivatedAbilities()).hasSize(1);
        assertThat(effect.additionalActivatedAbilities().getFirst().getDescription())
                .contains("Destroy target creature with the same name");
    }

    // ===== Copying a creature =====

    @Test
    @DisplayName("Evil Twin copies a creature and gains the destroy ability")
    void copiesCreatureAndGainsDestroyAbility() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EvilTwin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        Permanent evilTwinPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Evil Twin"))
                .findFirst().orElse(null);

        assertThat(evilTwinPerm).isNotNull();
        assertThat(evilTwinPerm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(evilTwinPerm.getCard().getPower()).isEqualTo(2);
        assertThat(evilTwinPerm.getCard().getToughness()).isEqualTo(2);
        // Should have the extra activated ability from Evil Twin
        assertThat(evilTwinPerm.getCard().getActivatedAbilities()).isNotEmpty();
        assertThat(evilTwinPerm.getCard().getActivatedAbilities()).anyMatch(a ->
                a.getDescription().contains("Destroy target creature with the same name"));
    }

    // ===== Activated ability — destroy same-name creature =====

    @Test
    @DisplayName("Evil Twin's activated ability destroys a creature with the same name")
    void activatedAbilityDestroysSameNameCreature() {
        // Put two Grizzly Bears on the field: one for player2, and Evil Twin copying it for player1
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EvilTwin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Find Evil Twin's permanent (which is named "Grizzly Bears")
        Permanent evilTwinPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Evil Twin"))
                .findFirst().orElse(null);
        assertThat(evilTwinPerm).isNotNull();
        evilTwinPerm.setSummoningSick(false);

        int evilTwinIndex = gd.playerBattlefields.get(player1.getId()).indexOf(evilTwinPerm);

        // Add mana for the ability
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Find the ability index (it may be the only ability or may follow copied abilities)
        int destroyAbilityIndex = -1;
        for (int i = 0; i < evilTwinPerm.getCard().getActivatedAbilities().size(); i++) {
            if (evilTwinPerm.getCard().getActivatedAbilities().get(i).getDescription().contains("Destroy target creature with the same name")) {
                destroyAbilityIndex = i;
                break;
            }
        }
        assertThat(destroyAbilityIndex).isGreaterThanOrEqualTo(0);

        // Activate the destroy ability targeting the original Grizzly Bears
        UUID targetBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, evilTwinIndex, destroyAbilityIndex, null, targetBearsId);

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        // Resolve the ability
        harness.passBothPriorities();

        // Original Grizzly Bears should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Evil Twin should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getOriginalCard().getName().equals("Evil Twin"));
    }

    // ===== Target restriction — cannot target different-name creature =====

    @Test
    @DisplayName("Evil Twin's ability cannot target a creature with a different name")
    void cannotTargetDifferentNameCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.setHand(player1, List.of(new EvilTwin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        Permanent evilTwinPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Evil Twin"))
                .findFirst().orElse(null);
        assertThat(evilTwinPerm).isNotNull();
        evilTwinPerm.setSummoningSick(false);

        int evilTwinIndex = gd.playerBattlefields.get(player1.getId()).indexOf(evilTwinPerm);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int destroyAbilityIndex = -1;
        for (int i = 0; i < evilTwinPerm.getCard().getActivatedAbilities().size(); i++) {
            if (evilTwinPerm.getCard().getActivatedAbilities().get(i).getDescription().contains("Destroy target creature with the same name")) {
                destroyAbilityIndex = i;
                break;
            }
        }
        assertThat(destroyAbilityIndex).isGreaterThanOrEqualTo(0);

        // Try to target Air Elemental (different name) — should fail
        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        final int abilityIdx = destroyAbilityIndex;
        assertThatThrownBy(() -> harness.activateAbility(player1, evilTwinIndex, abilityIdx, null, airElementalId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Declining to copy =====

    @Test
    @DisplayName("Evil Twin enters as 0/0 and dies when player declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EvilTwin()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Evil Twin should be dead (0/0 killed by SBA)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Evil Twin"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Evil Twin"));
    }
}
