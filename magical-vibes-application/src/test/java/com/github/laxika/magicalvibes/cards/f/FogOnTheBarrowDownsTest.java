package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FogOnTheBarrowDowns.class, GrizzlyBears.class, Spellbook.class})
class FogOnTheBarrowDownsTest extends BaseCardTest {

    @Test
    void enchantedCreatureBecomesOnlyASpirit() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachAura(bears);

        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.SPIRIT);
    }

    @Test
    void enchantedCreatureCannotAttack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachAura(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(bears);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent spellbook = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new FogOnTheBarrowDowns()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, spellbook.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void removingAuraRestoresCreatureTypeAndCombat() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = attachAura(bears);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).containsExactly(CardSubtype.BEAR);
        declareAttackers(player1, List.of(0));
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new FogOnTheBarrowDowns());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
