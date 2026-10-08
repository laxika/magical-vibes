package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YuffieMateriaHunter.class, GrizzlyBears.class, LeoninScimitar.class})
class YuffieMateriaHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a noncreature artifact and may attach it to Yuffie")
    void gainsControlAndAttachesEquipment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(equipment.getId());

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent yuffie = findPermanent(player1, "Yuffie, Materia Hunter");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isEqualTo(yuffie.getId());
    }

    @Test
    @DisplayName("Declining the Equipment attachment leaves the stolen artifact unattached")
    void mayDeclineEquipmentAttachment() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(equipment.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Returns the artifact when Yuffie leaves the battlefield")
    void controlEndsWhenYuffieLeaves() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(equipment.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        Permanent yuffie = findPermanent(player1, "Yuffie, Materia Hunter");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yuffie));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Yuffie onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YuffieMateriaHunter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent yuffie = findPermanent(player1, "Yuffie, Materia Hunter");
        assertThat(yuffie.isTapped()).isTrue();
        assertThat(yuffie.isAttacking()).isTrue();
        assertThat(yuffie.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target a creature with Yuffie's enter-the-battlefield ability")
    void targetMustBeNoncreatureArtifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YuffieMateriaHunter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature artifact");
    }

    @Test
    @DisplayName("Control gain and optional attachment resolve as one ability without a priority window")
    void controlAndAttachmentResolveTogether() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(equipment.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(equipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Yuffie, Materia Hunter").getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May attach a different Equipment already under your control")
    void mayChooseDifferentEquipment() {
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent stolenEquipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(stolenEquipment.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ownEquipment.getId());

        assertThat(ownEquipment.getAttachedTo()).isEqualTo(findPermanent(player1, "Yuffie, Materia Hunter").getId());
        assertThat(stolenEquipment.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(stolenEquipment);
    }

    @Test
    @DisplayName("An illegal artifact target prevents both control gain and attachment")
    void illegalTargetPreventsAttachment() {
        Permanent ownEquipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(ownEquipment.getAttachedTo()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No artifact control is gained if Yuffie leaves before her ability resolves")
    void sourceLeavesBeforeResolution() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(equipment.getId());
        harness.passBothPriorities();
        Permanent yuffie = findPermanent(player1, "Yuffie, Materia Hunter");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yuffie));
        resolveAllTriggers();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    private void castYuffie(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new YuffieMateriaHunter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetId);
    }
}
