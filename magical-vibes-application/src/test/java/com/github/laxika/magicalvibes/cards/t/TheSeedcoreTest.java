package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheSeedcoreTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent seedcore = addSeedcoreReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(seedcore.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds mana restricted to Phyrexian creature spells")
    void addsPhyrexianCreatureMana() {
        addSeedcoreReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.getSubtypeCreatureManaForColor(Set.of(CardSubtype.PHYREXIAN), ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Phyrexian creature mana can cast a Phyrexian creature")
    void phyrexianCreatureManaCastsPhyrexianCreature() {
        addSeedcoreReady(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(createCreature(
                "Test Phyrexian", "{R}", CardColor.RED, CardSubtype.PHYREXIAN)));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Phyrexian creature mana cannot cast a non-Phyrexian or noncreature spell")
    void phyrexianCreatureManaCannotCastOtherSpells() {
        addSeedcoreReady(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.setHand(player1, List.of(new LlanowarElves()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Card noncreaturePhyrexian = new Card();
        noncreaturePhyrexian.setName("Test Phyrexian Spell");
        noncreaturePhyrexian.setType(CardType.INSTANT);
        noncreaturePhyrexian.setManaCost("{G}");
        noncreaturePhyrexian.setColor(CardColor.GREEN);
        noncreaturePhyrexian.setSubtypes(List.of(CardSubtype.PHYREXIAN));
        harness.setHand(player1, List.of(noncreaturePhyrexian));
        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Corrupted ability boosts a 1/1 creature when an opponent has three poison counters")
    void corruptedBoostsOneOneCreature() {
        addSeedcoreReady(player1);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, elves.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(2);
    }

    @Test
    @DisplayName("Corrupted ability cannot be activated without a poisoned opponent")
    void corruptedRequiresPoisonedOpponent() {
        addSeedcoreReady(player1);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, elves.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("An opponent must have at least 3 poison counters");
    }

    @Test
    @DisplayName("Corrupted ability cannot target a creature that is not 1/1")
    void corruptedCannotTargetNonOneOneCreature() {
        addSeedcoreReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    @DisplayName("Corrupted boost wears off at end of turn")
    void corruptedBoostWearsOff() {
        addSeedcoreReady(player1);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, 2, null, elves.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
    }

    private Permanent addSeedcoreReady(Player player) {
        Permanent seedcore = new Permanent(new TheSeedcore());
        seedcore.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(seedcore);
        return seedcore;
    }
}
