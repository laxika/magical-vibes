package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverShroudCostume.class, GrizzlyBears.class, Disenchant.class})
class SilverShroudCostumeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Silver Shroud Costume attaches it and grants temporary shroud")
    void enteringAttachesAndGrantsTemporaryShroud() {
        Permanent creature = addCreatureReady(player1);
        castSilverShroudCostume(creature);

        Permanent costume = findPermanent(player1, "Silver Shroud Costume");
        assertThat(costume.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Entry shroud expires at end of turn while unblockability remains")
    void entryShroudExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        castSilverShroudCostume(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Equip {3} attaches Silver Shroud Costume to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent costume = addCostumeReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(costume.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Silver Shroud Costume cannot target an opponent's creature when entering")
    void cannotTargetOpponentsCreature() {
        Permanent ownCreature = addCreatureReady(player1);
        Permanent opponentCreature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownCreature.getId());
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Silver Shroud Costume").getAttachedTo())
                .isEqualTo(ownCreature.getId());
    }

    @Test
    @DisplayName("Flash allows Silver Shroud Costume to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canResolveWithoutAnyCreatureToAttachTo() {
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Silver Shroud Costume").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void entryAbilityGrantsShroudEvenIfEquipmentIsDestroyedInResponse() {
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent costume = findPermanent(player1, "Silver Shroud Costume");
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
        assertThat(costume.getAttachedTo()).isNull();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, costume.getId());
        harness.assertNotOnBattlefield(player1, "Silver Shroud Costume");
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    @Test
    void movingEquipmentDoesNotMoveTheEntryShroudGrant() {
        Permanent firstCreature = addCreatureReady(player1);
        Permanent secondCreature = addCreatureReady(player1);
        castSilverShroudCostume(firstCreature);
        Permanent costume = findPermanent(player1, "Silver Shroud Costume");
        int costumeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(costume);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, costumeIndex, null, secondCreature.getId());
        resolveAllTriggers();

        assertThat(costume.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, firstCreature)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, secondCreature)).isTrue();
    }

    @Test
    void equipCannotTargetACreatureWithEntryShroud() {
        Permanent creature = addCreatureReady(player1);
        castSilverShroudCostume(creature);
        Permanent costume = findPermanent(player1, "Silver Shroud Costume");
        int costumeIndex = gd.playerBattlefields.get(player1.getId()).indexOf(costume);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, costumeIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equipCannotBeActivatedDuringCombatDespiteFlash() {
        addCostumeReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void equippedCreatureCannotBeAssignedABlocker() {
        Permanent creature = addCreatureReady(player1);
        castSilverShroudCostume(creature);
        Permanent blocker = addCreatureReady(player2);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        declareAttackersAndPrepareBlockers(List.of(attackerIndex));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void attachingAfterBlockersAreDeclaredDoesNotUndoTheBlock() {
        Permanent creature = addCreatureReady(player1);
        Permanent blocker = addCreatureReady(player2);
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));

        harness.castArtifact(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
        assertThat(blocker.getBlockingTargetIds()).contains(creature.getId());
        resolveCombat();
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castSilverShroudCostume(Permanent target) {
        harness.setHand(player1, List.of(new SilverShroudCostume()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, target.getId());
        resolveAllTriggers();
    }

    private Permanent addCostumeReady(Player player) {
        return addCreatureReady(player, new SilverShroudCostume());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
