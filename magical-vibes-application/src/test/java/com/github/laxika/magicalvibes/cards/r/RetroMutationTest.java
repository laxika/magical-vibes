package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RetroMutation.class, SerraAngel.class, FountainOfYouth.class})
class RetroMutationTest extends BaseCardTest {

    @Test
    void transformsEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castRetroMutation(angel);

        assertThat(gqs.getEffectivePower(gd, angel)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.TURTLE);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
    }

    @Test
    void enchantedCreatureCannotAttack() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent aura = new Permanent(new RetroMutation());
        aura.setAttachedTo(angel.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void removingAuraRestoresEnchantedCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent aura = new Permanent(new RetroMutation());
        aura.setAttachedTo(angel.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RetroMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castRetroMutation(Permanent target) {
        harness.setHand(player1, List.of(new RetroMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
