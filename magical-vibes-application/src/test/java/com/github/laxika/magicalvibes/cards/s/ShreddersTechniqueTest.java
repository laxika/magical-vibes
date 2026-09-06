package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VernalEquinox;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShreddersTechnique.class, Forest.class, GrizzlyBears.class, VernalEquinox.class})
class ShreddersTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature without making its controller lose life")
    void destroysCreatureWithoutLifeLoss() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castNormally(target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Destroys an enchantment and its controller loses 2 life")
    void destroysEnchantmentAndLosesLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernalEquinox());
        castNormally(target.getId());

        harness.assertInGraveyard(player2, "Vernal Equinox");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and casts for {B}")
    void sneaksAndDestroysCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNormally(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShreddersTechnique()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
