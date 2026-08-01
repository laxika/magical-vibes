package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HonorablePassageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Honorable Passage prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castPassage(player1);
        addReady(player2, new HillGiant());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents a red source's damage and deals that much to its controller")
    void preventsRedSourceAndDamagesController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPassage(player1);
        Permanent giant = addReady(player2, new HillGiant());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        giant.setAttacking(true);
        resolveCombat(player2);

        // 3 combat damage prevented; Honorable Passage deals 3 to player2 (red source)
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents a non-red source's damage without damaging its controller")
    void preventsNonRedSourceWithoutDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castPassage(player1);
        Permanent bears = addReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        bears.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("Prevents the chosen red source's noncombat damage to a creature and damages its controller")
    void preventsNoncombatDamageToCreatureFromRedSource() {
        harness.setLife(player2, 20);
        castPassage(player1);
        Permanent pyromancer = addReady(player2, new ProdigalPyromancer());
        Permanent victim = addReady(player1, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, indexOf(player2, pyromancer), null, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(0);
        harness.assertLife(player2, 19);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castPassage(player1);
        Permanent chosen = addReady(player2, new HillGiant());
        Permanent other = addReady(player2, new GrizzlyBears());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.sourceNextDamageToAnyTargetShields)
                .extracting(s -> s.sourceId())
                .containsExactly(chosen.getId());
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castPassage(player1);
        Permanent giant = addReady(player2, new HillGiant());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, giant.getId());

        assertThat(gd.sourceNextDamageToAnyTargetShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    private void castPassage(Player player) {
        harness.setHand(player, List.of(new HonorablePassage()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castInstant(player, 0);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
