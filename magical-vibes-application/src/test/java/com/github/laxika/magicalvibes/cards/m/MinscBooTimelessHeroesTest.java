package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinscBooTimelessHeroes.class, GrizzlyBears.class, RagingGoblin.class})
class MinscBooTimelessHeroesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a legendary Boo with trample and haste")
    void enteringCreatesBoo() {
        enterMinscAndCreateBoo();

        Permanent boo = findPermanent(player1, "Boo");
        assertThat(boo.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(boo.getCard().getSubtypes()).contains(CardSubtype.HAMSTER);
        assertThat(boo.getEffectivePower()).isEqualTo(1);
        assertThat(boo.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, boo, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, boo, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The +1 ability puts three counters on a creature with haste")
    void plusOnePutsCountersOnCreatureWithHaste() {
        Permanent minsc = addReadyMinsc(3);
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());

        harness.activateAbility(player1, 0, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(minsc.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The +1 ability rejects a creature without trample or haste")
    void plusOneRejectsCreatureWithoutTrampleOrHaste() {
        addReadyMinsc(3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The -2 ability deals sacrificed power damage and draws for a Hamster")
    void minusTwoSacrificesHamsterDealsDamageAndDraws() {
        Permanent minsc = enterMinscAndCreateBoo();
        Permanent boo = findPermanent(player1, "Boo");
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, boo.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(minsc.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Boo");
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The upkeep trigger may create another Boo")
    void upkeepCreatesBoo() {
        addReadyMinsc(3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Boo")).hasSize(1);
    }

    private Permanent enterMinscAndCreateBoo() {
        harness.setHand(player1, List.of(new MinscBooTimelessHeroes()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent minsc = findPermanent(player1, "Minsc & Boo, Timeless Heroes");
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        return minsc;
    }

    private Permanent addReadyMinsc(int loyalty) {
        Permanent minsc = harness.addToBattlefieldAndReturn(player1, new MinscBooTimelessHeroes());
        minsc.setCounterCount(CounterType.LOYALTY, loyalty);
        minsc.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return minsc;
    }
}
