package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvilTwin.class, GrizzlyBears.class, AirElemental.class, AmbushViper.class})
class EvilTwinTest extends BaseCardTest {

    @Test
    @DisplayName("Evil Twin dies without a creature available to copy")
    void diesWithoutCreatureToCopy() {
        harness.castFromHand(player1, new EvilTwin(), "{2}{U}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Evil Twin");
        harness.assertInGraveyard(player1, "Evil Twin");
    }

    @Test
    @DisplayName("Evil Twin can copy its controller's creature and destroy itself")
    void canCopyOwnCreatureAndDestroyItself() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new AmbushViper());
        harness.castFromHand(player1, new EvilTwin(), "{2}{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());

        Permanent twin = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard() instanceof EvilTwin)
                .findFirst().orElseThrow();
        twin.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(twin),
                0, null, twin.getId());
        assertThat(twin.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original).doesNotContain(twin);
        harness.assertInGraveyard(player1, "Evil Twin");
        harness.assertNotInGraveyard(player1, "Ambush Viper");
    }

    @Test
    @DisplayName("Evil Twin's gained tap ability cannot be used while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new AmbushViper());
        harness.castFromHand(player1, new EvilTwin(), "{2}{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, original.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Ambush Viper");
    }

    @Test
    @DisplayName("Evil Twin copies a creature and gains the destroy ability")
    void copiesCreatureAndGainsDestroyAbility() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new EvilTwin(), "{2}{U}{B}");
        harness.passBothPriorities(); // Resolve the spell and request the copy choice.
        harness.passBothPriorities();

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

    @Test
    @DisplayName("Evil Twin's activated ability destroys a creature with the same name")
    void activatedAbilityDestroysSameNameCreature() {
        // Put two Grizzly Bears on the field: one for player2, and Evil Twin copying it for player1
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new EvilTwin(), "{2}{U}{B}");
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
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Evil Twin should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getOriginalCard().getName().equals("Evil Twin"));
    }

    @Test
    @DisplayName("Evil Twin's ability cannot target a creature with a different name")
    void cannotTargetDifferentNameCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new AirElemental());
        harness.castFromHand(player1, new EvilTwin(), "{2}{U}{B}");
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

        // A different-name creature is not a legal target.
        UUID airElementalId = harness.getPermanentId(player2, "Air Elemental");
        final int abilityIdx = destroyAbilityIndex;
        assertThatThrownBy(() -> harness.activateAbility(player1, evilTwinIndex, abilityIdx, null, airElementalId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Evil Twin enters as 0/0 and dies when player declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new EvilTwin(), "{2}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Evil Twin should be dead (0/0 killed by SBA)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Evil Twin"));
        harness.assertInGraveyard(player1, "Evil Twin");
    }
}
