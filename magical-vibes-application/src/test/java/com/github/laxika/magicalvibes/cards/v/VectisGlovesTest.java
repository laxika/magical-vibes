package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VectisGloves.class, GrizzlyBears.class, AncientDen.class, Ornithopter.class})
class VectisGlovesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0")
    void equippedCreatureGetsPowerBonus() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gloves.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Artifact landwalk prevents blocking while defending player controls an artifact land")
    void artifactLandwalkPreventsBlocking() {
        harness.addToBattlefield(player2, new AncientDen());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = equippedAttacker();

        prepareDeclareBlockers(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Artifact landwalk does not apply to a nonland artifact")
    void nonlandArtifactDoesNotGrantArtifactLandwalk() {
        harness.addToBattlefield(player2, new Ornithopter());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = equippedAttacker();
        harness.setLife(player2, 20);

        prepareDeclareBlockers(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Equip attaches Vectis Gloves to another creature")
    void equipAttachesToAnotherCreature() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gloves.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent equippedAttacker() {
        Permanent gloves = harness.addToBattlefieldAndReturn(player1, new VectisGloves());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        gloves.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        return attacker;
    }

    private void prepareDeclareBlockers(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
