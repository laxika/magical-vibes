package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.c.ConverterBeast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwakenedSkyclave.class, ConverterBeast.class, Forest.class, InvasionOfZendikar.class,
        Plains.class})
class InvasionOfZendikarTest extends BaseCardTest {

    @Test
    void searchesForUpToTwoBasicLandsAndPutsThemOntoTheBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new ConverterBeast()));

        harness.castFromHand(player1, new InvasionOfZendikar(), "{3}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.LAND));

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    void mayFindNoLandsEvenWhenTwoAreAvailable() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.castFromHand(player1, new InvasionOfZendikar(), "{3}{G}");
        resolveAllTriggers();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> gqs.isLand(gd, permanent));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayStopAfterFindingOneLand() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.castFromHand(player1, new InvasionOfZendikar(), "{3}{G}");
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> gqs.isLand(gd, permanent))
                .hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWhenLibraryContainsNoBasicLands() {
        harness.setLibrary(player1, List.of(new ConverterBeast()));
        harness.castFromHand(player1, new InvasionOfZendikar(), "{3}{G}");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> gqs.isLand(gd, permanent));
    }

    @Test
    void findsTheOnlyBasicLandWithoutRequiringASecondPick() {
        harness.setLibrary(player1, List.of(new Forest(), new ConverterBeast()));
        harness.castFromHand(player1, new InvasionOfZendikar(), "{3}{G}");
        resolveAllTriggers();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> gqs.isLand(gd, permanent))
                .hasSize(1).allMatch(Permanent::isTapped);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void controllerMayDeclineToCastTheDefeatedSiege() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void awakenedSkyclaveCanAttackImmediatelyWithoutTapping() {
        Permanent skyclave = harness.addToBattlefieldAndReturn(player1, new AwakenedSkyclave());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(skyclave.isTapped()).isFalse();
        harness.assertLife(player2, 16);
    }

    @Test
    void defeatingTheSiegeCastsAwakenedSkyclaveTransformed() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 0);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent skyclave = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AwakenedSkyclave)
                .findFirst()
                .orElseThrow();
        assertThat(skyclave.isTransformed()).isTrue();
        assertThat(gqs.isLand(gd, skyclave)).isTrue();

        int skyclaveIndex = gd.playerBattlefields.get(player1.getId()).indexOf(skyclave);
        harness.activateAbility(player1, skyclaveIndex, null, null);
        harness.handleListChoice(player1, "BLUE");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
