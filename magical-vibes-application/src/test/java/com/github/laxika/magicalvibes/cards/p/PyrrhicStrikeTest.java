package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PyrrhicStrikeTest extends BaseCardTest {

    private void addStrikeMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Without blight, destroys one target artifact or enchantment")
    void destroysArtifactWithoutBlight() {
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("Blight 2 puts counters on a creature and resolves both modes")
    void blightResolvesBothModes() {
        Permanent costCreature = addCreatureReady(player1, new HillGiant());
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        harness.ensurePriority(player1);
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}),
                null, null, List.of(artifact.getId(), creature.getId()), List.of(), false, costCreature.getId());

        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact, creature);
    }

    @Test
    @DisplayName("Cannot choose both modes without paying blight")
    void cannotChooseBothModesWithoutBlight() {
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("optional cost");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact, creature);
    }

    @Test
    @DisplayName("Cannot pay blight while choosing only one mode")
    void cannotPayBlightForOneMode() {
        Permanent costCreature = addCreatureReady(player1, new HillGiant());
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0}), null, null,
                List.of(artifact.getId()), List.of(), false, costCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("all modes");

        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
