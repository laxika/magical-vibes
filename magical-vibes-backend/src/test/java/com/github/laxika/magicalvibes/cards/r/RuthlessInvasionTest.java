package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RuthlessInvasionTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has SPELL effect with CantBlockThisTurnEffect(PermanentNotPredicate(PermanentIsArtifactPredicate))")
    void hasCorrectStructure() {
        RuthlessInvasion card = new RuthlessInvasion();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(CantBlockThisTurnEffect.class);
        CantBlockThisTurnEffect effect =
                (CantBlockThisTurnEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.filter()).isInstanceOf(PermanentNotPredicate.class);
        PermanentNotPredicate notPredicate = (PermanentNotPredicate) effect.filter();
        assertThat(notPredicate.predicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Effect resolution =====

    @Test
    @DisplayName("Nonartifact creatures can't block this turn after resolution")
    void nonartifactCreaturesCantBlock() {
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Artifact creatures are NOT affected")
    void artifactCreaturesNotAffected() {
        Permanent myr = addReadyCreature(player2, new IronMyr());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(myr.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Affects both players' nonartifact creatures")
    void affectsBothPlayersNonartifactCreatures() {
        Permanent ownBears = addReadyCreature(player1, new GrizzlyBears());
        Permanent oppBears = addReadyCreature(player2, new GrizzlyBears());
        Permanent oppMyr = addReadyCreature(player2, new IronMyr());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ownBears.isCantBlockThisTurn()).isTrue();
        assertThat(oppBears.isCantBlockThisTurn()).isTrue();
        assertThat(oppMyr.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Nonartifact creature cannot declare as blocker after resolution")
    void cantBlockPreventsDeclaringBlockers() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Artifact creature CAN still block after resolution")
    void artifactCreatureCanStillBlock() {
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new IronMyr());

        harness.setHand(player1, List.of(new RuthlessInvasion()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
