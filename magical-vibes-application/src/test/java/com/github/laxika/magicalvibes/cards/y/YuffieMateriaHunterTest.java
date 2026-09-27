package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
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

        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(equipment);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Returns the artifact when Yuffie leaves the battlefield")
    void controlEndsWhenYuffieLeaves() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        castYuffie(equipment.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
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
        declareAttackers(List.of(0));

        harness.setHand(player1, List.of(new YuffieMateriaHunter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
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

    private void castYuffie(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new YuffieMateriaHunter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetId);
    }
}
