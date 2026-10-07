package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndercoverOperative.class, GrizzlyBears.class, ChromeCat.class})
class UndercoverOperativeTest extends BaseCardTest {

    @Test
    void copiesAControlledCreatureWithAShieldCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castUndercoverOperative();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        Permanent operative = findOperative();
        assertThat(operative).isNotNull();
        assertThat(operative.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(operative.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void copiesAnOpponentsCreatureWithoutAShieldCounter() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castUndercoverOperative();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent operative = findOperative();
        assertThat(operative).isNotNull();
        assertThat(operative.getCounterCount(CounterType.SHIELD)).isZero();
    }

    @Test
    void decliningToCopyLeavesTheZeroZeroInTheGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castUndercoverOperative();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() instanceof UndercoverOperative);
        harness.assertInGraveyard(player1, "Undercover Operative");
    }

    @Test
    void noCreaturesToCopyLeavesOperativeInTheGraveyard() {
        castUndercoverOperative();

        harness.assertNotOnBattlefield(player1, "Undercover Operative");
        harness.assertInGraveyard(player1, "Undercover Operative");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void copiesEnterAbilityButNotCountersOrTappedStatus() {
        Permanent cat = harness.addToBattlefieldAndReturn(player1, new ChromeCat());
        cat.setCounterCount(CounterType.SHIELD, 3);
        cat.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        cat.tap();
        ChromeCat top = new ChromeCat();
        harness.setLibrary(player1, List.of(top));
        castUndercoverOperative();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, cat.getId());
        resolveAllTriggers();

        Permanent operative = findOperative();
        assertThat(operative).isNotNull();
        assertThat(operative.isTapped()).isFalse();
        assertThat(operative.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
        assertThat(operative.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
    }

    @Test
    void copyingAnOpponentsCreatureDoesNotCopyItsShieldCounters() {
        Permanent cat = harness.addToBattlefieldAndReturn(player2, new ChromeCat());
        cat.setCounterCount(CounterType.SHIELD, 3);
        harness.setLibrary(player1, List.of());
        castUndercoverOperative();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, cat.getId());
        resolveAllTriggers();

        Permanent operative = findOperative();
        assertThat(operative).isNotNull();
        assertThat(operative.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(cat.getCounterCount(CounterType.SHIELD)).isEqualTo(3);
    }

    private void castUndercoverOperative() {
        harness.setHand(player1, List.of(new UndercoverOperative()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private Permanent findOperative() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof UndercoverOperative)
                .findFirst()
                .orElse(null);
    }
}
