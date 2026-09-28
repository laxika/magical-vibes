package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimReapersScythe.class, GrizzlyBears.class, HolyDay.class})
class GrimReapersScytheTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature with a finality counter and creates a Zombie")
    void returnsCreatureWithFinalityCounterAndCreatesZombie() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new GrimReapersScythe());
        addCreatureReady(new GrizzlyBears());
        addCreatureReady(new GrizzlyBears());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        prepareMainPhase();
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scythe), 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(target);
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(scythe.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("A returned creature with a finality counter is exiled instead of dying")
    void finalityExilesReturnedCreatureWhenItDies() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new GrimReapersScythe());
        addCreatureReady(new GrizzlyBears());
        addCreatureReady(new GrizzlyBears());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        prepareMainPhase();
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scythe), 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent returned = findPermanentByCardId(target);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, returned));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot activate outside sorcery timing")
    void cannotActivateOutsideSorceryTiming() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new GrimReapersScythe());
        Permanent firstCost = addCreatureReady(new GrizzlyBears());
        Permanent secondCost = addCreatureReady(new GrizzlyBears());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scythe), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(scythe.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstCost, secondCost);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent scythe = harness.addToBattlefieldAndReturn(player1, new GrimReapersScythe());
        addCreatureReady(new GrizzlyBears());
        addCreatureReady(new GrizzlyBears());
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        prepareMainPhase();
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scythe), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addCreatureReady(Card card) {
        return addCreatureReady(player1, card);
    }

    private Permanent findPermanentByCardId(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
