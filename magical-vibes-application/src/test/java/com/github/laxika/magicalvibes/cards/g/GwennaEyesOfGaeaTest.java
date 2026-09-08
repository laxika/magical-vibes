package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GwennaEyesOfGaeaTest extends BaseCardTest {

    @Test
    @DisplayName("Gwenna adds two independently chosen creature-spell-or-creature-ability mana")
    void manaAbilityAddsRestrictedMana() {
        Permanent gwenna = harness.addToBattlefieldAndReturn(player1, new GwennaEyesOfGaea());
        gwenna.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.RED)).isZero();
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.RED)).isEqualTo(1);
        assertThat(pool.getCreatureSpellOrAbilityMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gwenna mana pays for creature spells and creature-source abilities")
    void manaPaysForCreaturesAndCreatureAbilities() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOrAbilityMana(ManaColor.RED, 1);
        Card creature = createCreature("Test Creature", "{R}", CardColor.RED, 5);
        harness.setHand(player1, List.of(creature));

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(pool.getCreatureSpellOrAbilityManaTotal()).isZero();

        Card creatureWithAbility = createCreatureWithRedAbility("Creature Source");
        harness.addToBattlefield(player1, creatureWithAbility);
        pool.addCreatureSpellOrAbilityMana(ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(pool.getCreatureSpellOrAbilityManaTotal()).isZero();
    }

    @Test
    @DisplayName("Gwenna mana cannot pay for an ability of a noncreature source")
    void manaCannotPayNonCreatureAbility() {
        Card artifact = new Card();
        artifact.setName("Noncreature Source");
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("{2}");
        artifact.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(1)), "{R}: You gain 1 life."));
        harness.addToBattlefield(player1, artifact);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addCreatureSpellOrAbilityMana(ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pool.getCreatureSpellOrAbilityManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature with power five or greater adds a counter and untaps Gwenna")
    void powerfulCreatureAddsCounterAndUntapsGwenna() {
        Permanent gwenna = harness.addToBattlefieldAndReturn(player1, new GwennaEyesOfGaea());
        gwenna.setSummoningSick(false);
        gwenna.tap();

        Card creature = createCreature("Powerful Creature", "{5}", CardColor.GREEN, 5);
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gwenna.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gwenna.isTapped()).isFalse();
    }

    private static Card createCreature(String name, String manaCost, CardColor color, int power) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(5);
        return card;
    }

    private static Card createCreatureWithRedAbility(String name) {
        Card card = createCreature(name, "{2}", CardColor.RED, 2);
        card.addActivatedAbility(new ActivatedAbility(
                false, "{R}", List.of(new GainLifeEffect(1)), "{R}: You gain 1 life."));
        return card;
    }
}
