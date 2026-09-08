package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NemesisMaskTest extends BaseCardTest {

    @Test
    @DisplayName("All able creatures must block the equipped creature")
    void allAbleCreaturesMustBlockEquippedCreature() {
        Permanent attacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        Permanent mask = new Permanent(new NemesisMask());
        mask.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(mask);

        Permanent blocker1 = addCreatureReady(player2, new GrizzlyBears());
        Permanent blocker2 = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(blocker1.isBlocking()).isTrue();
        assertThat(blocker2.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Only creatures able to block the equipped creature are forced to block")
    void onlyAbleCreaturesAreForcedToBlock() {
        Permanent attacker = attackingCreature(new AvenFisher());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        Permanent mask = new Permanent(new NemesisMask());
        mask.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(mask);

        Permanent unableBlocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent ableBlocker = addCreatureReady(player2, new AvenFisher());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(unableBlocker.isBlocking()).isFalse();
        assertThat(ableBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Equipping Nemesis Mask attaches it to a creature")
    void equipAttachesMask() {
        Permanent mask = new Permanent(new NemesisMask());
        gd.playerBattlefields.get(player1.getId()).add(mask);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An unattached Nemesis Mask does not force blockers")
    void unattachedMaskDoesNotForceBlockers() {
        Permanent attacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new NemesisMask()));
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    private Permanent attackingCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }
}
