package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PyrrhicStrike.class, HillGiant.class, Ornithopter.class, GloriousAnthem.class, GrizzlyBears.class})
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

    @Test
    void destroysEnchantmentWithoutBlight() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void destroysLargeCreatureWithoutBlight() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @CardUsed({ScatheZombies.class})
    void creatureModeIncludesManaValueExactlyThree() {
        Permanent creature = addCreatureReady(player2, new ScatheZombies());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scathe Zombies");
    }

    @Test
    void creatureModeRejectsManaValueBelowThree() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void firstModeRejectsOrdinaryCreature() {
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void creatureModeRejectsNoncreatureWithManaValueThree() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
    }

    @Test
    void blightCannotUseOpponentsCreature() {
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), null, null,
                List.of(artifact.getId(), creature.getId()), List.of(), false, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void blightCanKillCostCreatureAndStillResolveBothModes() {
        Permanent costCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}),
                null, null, List.of(artifact.getId(), creature.getId()), List.of(), false, costCreature.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact, creature);
    }

    @Test
    @CardUsed({Solemnity.class})
    void cannotChooseBlightWhenCountersCannotBePlaced() {
        harness.addToBattlefield(player2, new Solemnity());
        Permanent costCreature = addCreatureReady(player1, new HillGiant());
        Permanent artifact = addCreatureReady(player2, new Ornithopter());
        Permanent creature = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new PyrrhicStrike()));
        addStrikeMana();
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}), null, null,
                List.of(artifact.getId(), creature.getId()), List.of(), false, costCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(costCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
