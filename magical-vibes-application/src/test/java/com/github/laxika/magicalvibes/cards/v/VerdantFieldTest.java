package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

class VerdantFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Verdant Field enchants a land")
    void enchantsLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new VerdantField()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, List.of(forest.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached() && permanent.getAttachedTo().equals(forest.getId()));
    }

    @Test
    @DisplayName("The enchanted land can tap to give a creature +1/+1")
    void enchantedLandBoostsTargetCreature() {
        Permanent forest = addReady(player1, new Forest());
        Permanent target = addReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantField());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn and noncreatures cannot be targeted")
    void boostWearsOffAndRejectsNoncreatureTarget() {
        Permanent forest = addReady(player1, new Forest());
        Permanent target = addReady(player1, new GrizzlyBears());
        Permanent otherLand = addReady(player1, new Forest());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VerdantField());
        aura.setAttachedTo(forest.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        forest.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
