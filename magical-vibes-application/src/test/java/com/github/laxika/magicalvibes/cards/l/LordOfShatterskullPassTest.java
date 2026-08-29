package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LordOfShatterskullPassTest extends BaseCardTest {

    @Test
    @DisplayName("Leveling up changes Lord of Shatterskull Pass's power and toughness")
    void levelsUpAtThresholds() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());

        assertStats(lord, 3, 3);

        prepareForLeveling(player1);
        levelUp(player1);

        assertThat(lord.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertStats(lord, 6, 6);

        for (int i = 0; i < 4; i++) {
            levelUp(player1);
        }

        assertThat(lord.getCounterCount(CounterType.LEVEL)).isEqualTo(5);
        assertStats(lord, 6, 6);

        levelUp(player1);

        assertThat(lord.getCounterCount(CounterType.LEVEL)).isEqualTo(6);
        assertStats(lord, 6, 6);
    }

    @Test
    @DisplayName("At level 6, attacking Lord of Shatterskull Pass deals 6 damage to each defending creature")
    void levelSixAttackTriggerDamagesDefendingCreatures() {
        Permanent lord = addCreatureReady(player1, new LordOfShatterskullPass());
        Permanent defendingCreature = addCreatureReady(player2, createCreature("Large Defender", 7, 7));
        Permanent ownCreature = addCreatureReady(player1, createCreature("Large Ally", 7, 7));

        prepareForLeveling(player1);
        for (int i = 0; i < 6; i++) {
            levelUp(player1);
        }

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingCreature.getMarkedDamage()).isEqualTo(6);
        assertThat(ownCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Lord of Shatterskull Pass's attack trigger is inactive below level 6")
    void attackTriggerIsInactiveBelowLevelSix() {
        addCreatureReady(player1, new LordOfShatterskullPass());
        Permanent defendingCreature = addCreatureReady(player2, createCreature("Large Defender", 7, 7));

        prepareForLeveling(player1);
        for (int i = 0; i < 5; i++) {
            levelUp(player1);
        }

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(defendingCreature.getMarkedDamage()).isZero();
    }

    private static Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private void prepareForLeveling(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.RED, 12);
    }

    private void levelUp(com.github.laxika.magicalvibes.model.Player player) {
        harness.activateAbility(player, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void assertStats(Permanent permanent, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(toughness);
    }
}
