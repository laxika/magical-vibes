package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MotherBear;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnsweredPrayers.class, MotherBear.class})
class AnsweredPrayersTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life and becomes a 3/3 Angel with flying when a creature enters")
    void gainsLifeAndBecomesAngelWhenCreatureEnters() {
        harness.setLife(player1, 20);
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gqs.isCreature(gd, prayers)).isTrue();
        assertThat(gqs.isEnchantment(gd, prayers)).isTrue();
        assertThat(gqs.getEffectivePower(gd, prayers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prayers)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, prayers)).contains(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, prayers, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Still gains life when already a creature")
    void stillGainsLifeWhenAlreadyCreature() {
        harness.setLife(player1, 20);
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.setHand(player1, List.of(new MotherBear(), new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gqs.isCreature(gd, prayers)).isTrue();
        assertThat(gqs.getEffectivePower(gd, prayers)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prayers)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.setHand(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, prayers)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, prayers)).isFalse();
        assertThat(gqs.isEnchantment(gd, prayers)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, prayers)).doesNotContain(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, prayers, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creatures do not trigger life gain or animation")
    void opponentsCreatureDoesNotTrigger() {
        harness.setLife(player1, 20);
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new MotherBear()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, prayers)).isFalse();
    }

    @Test
    @DisplayName("Entering as a noncreature enchantment does not trigger itself")
    void noncreatureEntryDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AnsweredPrayers()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
        Permanent prayers = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, prayers)).isFalse();
    }

    @Test
    @DisplayName("Two creature tokens entering together each trigger life gain")
    void eachTokenTriggersLifeGain() {
        harness.setLife(player1, 20);
        Permanent prayers = harness.addToBattlefieldAndReturn(player1, new AnsweredPrayers());
        harness.setGraveyard(player1, List.of(new MotherBear()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gqs.isCreature(gd, prayers)).isTrue();
        assertThat(gqs.hasKeyword(gd, prayers, Keyword.FLYING)).isTrue();
    }

    @Test
    @CardUsed(Opalescence.class)
    @DisplayName("Entering as a creature under Opalescence triggers its own life gain")
    void creatureEntryTriggersItself() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Opalescence());
        harness.setHand(player1, List.of(new AnsweredPrayers()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        Permanent prayers = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.isCreature(gd, prayers)).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gqs.effectiveCreatureSubtypes(gd, prayers)).doesNotContain(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, prayers, Keyword.FLYING)).isFalse();
    }
}
