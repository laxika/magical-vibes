package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WhirlerRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates two 1/1 flying Thopter artifact creature tokens")
    void createsTwoThopters() {
        harness.setHand(player1, List.of(new WhirlerRogue()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(thopters).hasSize(2);
        for (Permanent thopter : thopters) {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        }
    }

    @Test
    @DisplayName("Tapping two artifacts makes the target creature unblockable this turn")
    void abilityMakesTargetUnblockable() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        Permanent artifact1 = addCreatureReady(player1, new Ornithopter());
        Permanent artifact2 = addCreatureReady(player1, new Ornithopter());
        Permanent artifact3 = addCreatureReady(player1, new Ornithopter());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        int rogueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(rogue);
        harness.activateAbility(player1, rogueIdx, null, attacker.getId());
        harness.handlePermanentChosen(player1, artifact1.getId());
        harness.handlePermanentChosen(player1, artifact2.getId());
        harness.passBothPriorities();

        assertThat(artifact1.isTapped()).isTrue();
        assertThat(artifact2.isTapped()).isTrue();
        assertThat(artifact3.isTapped()).isFalse();

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the ability with only one untapped artifact")
    void cannotActivateWithOneArtifact() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        addCreatureReady(player1, new Ornithopter());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        int rogueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(rogue);

        assertThatThrownBy(() -> harness.activateAbility(player1, rogueIdx, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent rogue = addCreatureReady(player1, new WhirlerRogue());
        addCreatureReady(player1, new Ornithopter());
        addCreatureReady(player1, new Ornithopter());

        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);

        int rogueIdx = gd.playerBattlefields.get(player1.getId()).indexOf(rogue);

        assertThatThrownBy(() -> harness.activateAbility(player1, rogueIdx, null, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
