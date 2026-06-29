package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GaeasProtector;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GaeasProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Gaea's Protector has MustBeBlockedIfAbleEffect as static effect")
    void hasCorrectEffect() {
        GaeasProtector card = new GaeasProtector();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MustBeBlockedIfAbleEffect.class);
    }

    @Test
    @DisplayName("At least one creature must block Gaea's Protector if able")
    void mustBeBlockedByAtLeastOne() {
        Permanent protector = attackingCreature(new GaeasProtector());
        gd.playerBattlefields.get(player1.getId()).add(protector);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        // No blockers assigned — should fail because at least one must block
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Blocking with one creature satisfies the requirement")
    void oneBlockerSuffices() {
        Permanent protector = attackingCreature(new GaeasProtector());
        gd.playerBattlefields.get(player1.getId()).add(protector);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        // One blocker assigned — should succeed (unlike Lure which requires all)
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Gaea's Protector")
    void tappedCreaturesNotForcedToBlock() {
        Permanent protector = attackingCreature(new GaeasProtector());
        gd.playerBattlefields.get(player1.getId()).add(protector);

        Permanent tapped = readyCreature(new GrizzlyBears());
        tapped.tap();
        gd.playerBattlefields.get(player2.getId()).add(tapped);

        prepareDeclareBlockers();

        // Only blocker is tapped, so no block is required
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Defender can choose which creature blocks Gaea's Protector")
    void defenderChoosesWhichCreatureBlocks() {
        Permanent protector = attackingCreature(new GaeasProtector());
        gd.playerBattlefields.get(player1.getId()).add(protector);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        // Block with second creature instead of first — should succeed
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    private Permanent attackingCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
