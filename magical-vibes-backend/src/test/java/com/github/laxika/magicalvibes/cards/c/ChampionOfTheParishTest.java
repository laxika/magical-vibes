package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChampionOfTheParishTest extends BaseCardTest {

    @Test
    @DisplayName("Has ON_ALLY_CREATURE_ENTERS_BATTLEFIELD trigger with subtype HUMAN conditional")
    void hasCorrectProperties() {
        ChampionOfTheParish card = new ChampionOfTheParish();

        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst())
                .isInstanceOf(SubtypeConditionalEffect.class);

        SubtypeConditionalEffect conditional =
                (SubtypeConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD).getFirst();
        assertThat(conditional.subtype()).isEqualTo(CardSubtype.HUMAN);
        assertThat(conditional.wrapped()).isInstanceOf(PutCountersOnSourceEffect.class);

        PutCountersOnSourceEffect effect = (PutCountersOnSourceEffect) conditional.wrapped();
        assertThat(effect.powerModifier()).isEqualTo(1);
        assertThat(effect.toughnessModifier()).isEqualTo(1);
        assertThat(effect.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when another Human enters the battlefield")
    void getsCounterWhenHumanEnters() {
        harness.addToBattlefield(player1, new ChampionOfTheParish());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(champion.getPlusOnePlusOneCounters()).isZero();

        // Cast Elite Vanguard (Human Soldier)
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (triggers Champion)
        harness.passBothPriorities(); // resolve Champion's +1/+1 counter triggered ability

        assertThat(champion.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when a non-Human creature enters")
    void noCounterWhenNonHumanEnters() {
        harness.addToBattlefield(player1, new ChampionOfTheParish());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast Grizzly Bears (Bear, not Human)
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(champion.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when opponent casts a Human")
    void noCounterWhenOpponentCastsHuman() {
        harness.addToBattlefield(player1, new ChampionOfTheParish());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Opponent casts a Human
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new EliteVanguard()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(champion.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Gets multiple counters from multiple Human entries")
    void getsMultipleCounters() {
        harness.addToBattlefield(player1, new ChampionOfTheParish());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).getFirst();

        // Cast first Human
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Champion's triggered ability

        assertThat(champion.getPlusOnePlusOneCounters()).isEqualTo(1);

        // Cast second Human
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Champion's triggered ability

        assertThat(champion.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, champion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, champion)).isEqualTo(3);
    }
}
