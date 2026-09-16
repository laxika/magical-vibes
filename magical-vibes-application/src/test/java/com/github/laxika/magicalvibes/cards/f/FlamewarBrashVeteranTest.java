package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlamewarBrashVeteran.class, FlamewarStreetwiseOperative.class, MishrasBauble.class,
        GrizzlyBears.class, Mountain.class})
class FlamewarBrashVeteranTest extends BaseCardTest {

    @Test
    void moreThanMeetsTheEyeCastsFlamewarConvertedWithLivingMetal() {
        Permanent flamewar = castConvertedFlamewar();

        assertThat(flamewar.isTransformed()).isTrue();
        assertThat(flamewar.getCard()).isInstanceOf(FlamewarStreetwiseOperative.class);
        assertThat(gqs.isCreature(gd, flamewar)).isTrue();
    }

    @Test
    void sacrificingAnotherArtifactAddsCounterAndConvertsFlamewar() {
        Permanent flamewar = addCreatureReady(player1, new FlamewarBrashVeteran());
        Permanent bauble = harness.addToBattlefieldAndReturn(player1, new MishrasBauble());

        harness.activateAbility(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bauble);
        assertThat(flamewar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(flamewar.isTransformed()).isTrue();
    }

    @Test
    void combatDamageExilesThatManyCardsWithIntelCountersAndConvertsBack() {
        Permanent flamewar = castConvertedFlamewar();
        Card first = new GrizzlyBears();
        Card second = new Mountain();
        Card remaining = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, remaining));

        flamewar.setSummoningSick(false);
        flamewar.setAttacking(true);
        flamewar.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        gd.interaction.clearAwaitingInput();
        harness.resolveCombatDamage();
        harness.assertLife(player2, 18);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(flamewar.isTransformed()).isFalse();
        assertThat(gd.exiledCardsWithIntelCounters)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        for (Card card : List.of(first, second)) {
            ExiledCardEntry entry = gd.findExiledCard(card.getId());
            assertThat(entry).isNotNull();
            assertThat(entry.sourcePermanentId()).isEqualTo(flamewar.getId());
            assertThat(entry.faceDown()).isTrue();
        }
    }

    @Test
    void returnAbilityOnlyReturnsOwnedCardsWithIntelCounters() {
        Permanent flamewar = addCreatureReady(player1, new FlamewarBrashVeteran());
        Card marked = new GrizzlyBears();
        Card unmarked = new Mountain();
        Card opponentOwned = new GrizzlyBears();
        gd.addToExile(player1.getId(), marked, flamewar.getId(), true);
        gd.addToExile(player1.getId(), unmarked, flamewar.getId(), true);
        gd.addToExile(player2.getId(), opponentOwned, flamewar.getId(), true);
        gd.exiledCardsWithIntelCounters.add(marked.getId());
        gd.exiledCardsWithIntelCounters.add(opponentOwned.getId());
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(marked);
        assertThat(gd.findExiledCard(marked.getId())).isNull();
        assertThat(gd.findExiledCard(unmarked.getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentOwned.getId())).isNotNull();
    }

    private Permanent castConvertedFlamewar() {
        harness.setHand(player1, List.of(new FlamewarBrashVeteran()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Flamewar, Streetwise Operative");
    }
}
