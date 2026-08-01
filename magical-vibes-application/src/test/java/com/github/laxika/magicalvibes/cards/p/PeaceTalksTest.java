package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PeaceTalksTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures can't attack this turn after Peace Talks resolves")
    void creaturesCantAttackThisTurn() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castPeaceTalks(player1);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(2);
        assertThat(bear.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Restriction lasts through the next turn, then clears")
    void lastsThisTurnAndNextThenClears() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        castPeaceTalks(player1);
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(2);

        // Leave player1's turn → still active on player2's turn (1 remaining)
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(1);

        Permanent oppBear = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId())).isEmpty();
        assertThat(oppBear.isTapped()).isFalse();

        // Leave player2's turn → Peace Talks expires
        advanceTurn();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.peaceTalksTurnsRemaining).isEqualTo(0);
        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId()))
                .contains(indexOf(player1, bear));
    }

    @Test
    @DisplayName("Spells cannot target permanents while Peace Talks is active")
    void spellsCannotTargetPermanents() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        castPeaceTalks(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    @Test
    @DisplayName("Spells cannot target players while Peace Talks is active")
    void spellsCannotTargetPlayers() {
        castPeaceTalks(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    @Test
    @DisplayName("Activated abilities cannot target permanents while Peace Talks is active")
    void activatedAbilitiesCannotTargetPermanents() {
        Permanent victim = addCreatureReady(player1, new GrizzlyBears());
        Permanent pyro = addCreatureReady(player2, new ProdigalPyromancer());
        castPeaceTalks(player1);

        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, indexOf(player2, pyro), null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the targets");
    }

    private void castPeaceTalks(Player caster) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new PeaceTalks()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castSorcery(caster, 0, 0);
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }
}
