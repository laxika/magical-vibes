package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.t.TaureanMauler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroicReturn.class, HerculesOlympianHero.class, GrizzlyBears.class, HolyDay.class,
        TaureanMauler.class, GarruksPackleader.class})
class HeroicReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a Hero with two +1/+1 counters")
    void returnsHeroWithTwoCounters() {
        Card hero = new HerculesOlympianHero();
        harness.setGraveyard(player1, List.of(hero));
        harness.setHand(player1, List.of(new HeroicReturn()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0, hero.getId());

        Permanent returned = findOnBattlefield(hero);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a non-Hero creature without counters")
    void returnsNonHeroWithoutCounters() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent returned = findOnBattlefield(creature);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Costs two less while a creature is attacking you")
    void costsTwoLessWhileCreatureAttacksYou() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new HeroicReturn()));
        addFullMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    @Test
    void returnsChangelingWithHeroCounters() {
        Card creature = new TaureanMauler();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(findOnBattlefield(creature).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    void enteringPowerIncludesHeroCountersForPackleader() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        Card creature = new TaureanMauler();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetOpponentsCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        addFullMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresFullCostWithoutAnAttacker() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackingAnotherPlayerDoesNotReduceCost() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Card creature = new HerculesOlympianHero();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HeroicReturn()));
        addFullMana();
        harness.castInstant(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent findOnBattlefield(Card card) {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
