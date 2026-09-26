package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HerculesOlympianHero;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinterSoldierRebornAvenger.class, HerculesOlympianHero.class, GrizzlyBears.class, HillGiant.class})
class WinterSoldierRebornAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns a legal Hero with a +1/+1 counter")
    void attackingReturnsHeroWithCounter() {
        addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card hero = new HerculesOlympianHero();
        harness.setGraveyard(player1, List.of(hero));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(hero.getId());

        harness.handleMultipleCardsChosen(player1, List.of(hero.getId()));
        harness.passBothPriorities();

        Permanent returned = findOnBattlefield(hero);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking excludes creatures whose mana value exceeds Winter Soldier's power")
    void attackingExcludesTooExpensiveCreature() {
        addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
    }

    @Test
    @DisplayName("Attacking returns a non-Hero creature without a counter")
    void attackingReturnsNonHeroWithoutCounter() {
        addCreatureReady(player1, new WinterSoldierRebornAvenger());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = findOnBattlefield(creature);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent findOnBattlefield(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
