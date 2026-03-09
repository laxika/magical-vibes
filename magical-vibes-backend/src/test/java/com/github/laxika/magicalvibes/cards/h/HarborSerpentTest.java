package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HarborSerpentTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Harbor Serpent has CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect for 5 Islands")
    void hasCorrectEffect() {
        HarborSerpent card = new HarborSerpent();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect.class);
        CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect effect =
                (CantAttackUnlessBattlefieldHasMatchingPermanentCountEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.permanentPredicate()).isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.ISLAND));
        assertThat(effect.minimumCount()).isEqualTo(5);
    }

    // ===== Attack restriction =====

    @Test
    @DisplayName("Harbor Serpent can attack when there are exactly 5 Islands on the battlefield")
    void canAttackWithFiveIslands() {
        harness.setLife(player2, 20);
        // 3 Islands on player1's side, 2 on player2's side = 5 total
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = new Permanent(new HarborSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(findIndex(player1, serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Harbor Serpent can attack when there are more than 5 Islands on the battlefield")
    void canAttackWithMoreThanFiveIslands() {
        harness.setLife(player2, 20);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Island());
        }
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Island());
        }

        Permanent serpent = new Permanent(new HarborSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(findIndex(player1, serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Harbor Serpent cannot attack when there are fewer than 5 Islands on the battlefield")
    void cannotAttackWithFewerThanFiveIslands() {
        // 2 Islands on player1, 2 on player2 = only 4 total
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = new Permanent(new HarborSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        int serpentIndex = findIndex(player1, serpent);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(serpentIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Harbor Serpent cannot attack when there are no Islands on the battlefield")
    void cannotAttackWithNoIslands() {
        Permanent serpent = new Permanent(new HarborSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Islands on both sides of the battlefield count toward the five required")
    void islandsFromBothPlayersCounted() {
        harness.setLife(player2, 20);
        // 2 on player1's side, 3 on player2's side = 5 total
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent serpent = new Permanent(new HarborSerpent());
        serpent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serpent);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(findIndex(player1, serpent)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Helper =====

    private int findIndex(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i) == target) return i;
        }
        throw new IllegalStateException("Permanent not found on battlefield");
    }
}
