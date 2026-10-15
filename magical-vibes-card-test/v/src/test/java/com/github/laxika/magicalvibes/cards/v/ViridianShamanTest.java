package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.d.DarksteelPlate;
import com.github.laxika.magicalvibes.cards.e.EzurisArchers;
import com.github.laxika.magicalvibes.cards.e.EtheriumSculptor;
import com.github.laxika.magicalvibes.cards.g.GreatFurnace;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViridianShaman.class, GreatFurnace.class, EzurisArchers.class, DarksteelPlate.class,
        EtheriumSculptor.class})
class ViridianShamanTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Viridian Shaman needs no target even with artifacts present")
    void castingNeedsNoTargetWithArtifactsPresent() {
        harness.addToBattlefield(player2, new GreatFurnace());
        harness.castFromHand(player1, new ViridianShaman(), "{2}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Viridian Shaman");
        assertThat(entry.getTargetId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Viridian Shaman enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new GreatFurnace());
        harness.setHand(player1, List.of(new ViridianShaman()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Great Furnace");
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Viridian Shaman");

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Viridian Shaman");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new GreatFurnace());
        harness.setHand(player1, List.of(new ViridianShaman()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Great Furnace");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Great Furnace");
        harness.assertInGraveyard(player2, "Great Furnace");
    }

    @Test
    @DisplayName("Can destroy own artifact with ETB")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new GreatFurnace());
        harness.setHand(player1, List.of(new ViridianShaman()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Great Furnace");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Great Furnace");
        harness.assertInGraveyard(player1, "Great Furnace");
    }

    @Test
    @DisplayName("Can destroy an artifact creature with ETB")
    void canDestroyArtifactCreature() {
        harness.addToBattlefield(player2, new EtheriumSculptor());
        harness.setHand(player1, List.of(new ViridianShaman()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Etherium Sculptor");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Etherium Sculptor");
        harness.assertInGraveyard(player2, "Etherium Sculptor");
    }

    @Test
    @DisplayName("ETB fizzles if target artifact is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreatFurnace());
        harness.setHand(player1, List.of(new ViridianShaman()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        GameData gd = harness.getGameData();
        UUID targetId = target.getId();
        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Indestructible artifact survives the ETB destroy")
    void indestructibleArtifactSurvives() {
        harness.addToBattlefield(player2, new DarksteelPlate());
        harness.setHand(player1, List.of(new ViridianShaman()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Darksteel Plate");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Darksteel Plate");
        harness.assertNotInGraveyard(player2, "Darksteel Plate");
    }

    // ===== Target restriction =====

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new EzurisArchers());
        harness.addToBattlefield(player2, new GreatFurnace());
        harness.castFromHand(player1, new ViridianShaman(), "{2}{G}");
        harness.passBothPriorities();

        UUID targetId = harness.getPermanentId(player2, "Ezuri's Archers");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Great Furnace"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Ezuri's Archers");
        harness.assertInGraveyard(player2, "Great Furnace");
    }

    @Test
    @DisplayName("Chooses an artifact when the ETB ability is put on the stack")
    void choosesTargetWhenEtbIsPutOnStack() {
        harness.castFromHand(player1, new ViridianShaman(), "{2}{G}");
        harness.addToBattlefield(player2, new GreatFurnace());

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        UUID targetId = harness.getPermanentId(player2, "Great Furnace");
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Great Furnace");
        harness.assertInGraveyard(player2, "Great Furnace");
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no artifacts on battlefield")
    void canCastWithoutTargetWhenNoArtifacts() {
        harness.castFromHand(player1, new ViridianShaman(), "{2}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Viridian Shaman");
    }

    @Test
    @DisplayName("ETB ability is not put on the stack when no legal targets exist")
    void etbAbilityIsNotPutOnStackWithoutLegalTargets() {
        harness.castFromHand(player1, new ViridianShaman(), "{2}{G}");

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature should be on battlefield
        harness.assertOnBattlefield(player1, "Viridian Shaman");
        // No triggered ability on stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new GreatFurnace());
        harness.setHand(player1, List.of(new ViridianShaman()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID targetId = harness.getPermanentId(player2, "Great Furnace");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Entering without being cast still destroys an artifact")
    void enteringWithoutBeingCastTriggersDestruction() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GreatFurnace());

        harness.enterBattlefieldAndReturn(player1, new ViridianShaman());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Viridian Shaman");
        harness.assertNotOnBattlefield(player2, "Great Furnace");
        harness.assertInGraveyard(player2, "Great Furnace");
    }

    @Test
    @DisplayName("The trigger destroys its target even after Viridian Shaman leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GreatFurnace());
        harness.castFromHand(player1, new ViridianShaman(), "{2}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());

        Permanent shaman = findPermanent(player1, "Viridian Shaman");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, shaman));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Viridian Shaman");
        harness.assertNotOnBattlefield(player2, "Great Furnace");
        harness.assertInGraveyard(player2, "Great Furnace");
    }

    @Test
    @DisplayName("Must destroy its controller's artifact when that is the only legal target")
    void mustDestroyOnlyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GreatFurnace());
        harness.castFromHand(player1, new ViridianShaman(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Great Furnace");
        harness.assertInGraveyard(player1, "Great Furnace");
    }
}
