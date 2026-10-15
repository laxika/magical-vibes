package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({NissaWhoShakesTheWorld.class, Forest.class, PrimordialWurm.class, Plains.class})
class NissaWhoShakesTheWorldTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping your Forest adds an additional green mana")
    void ownForestProducesExtraGreen() {
        harness.addToBattlefield(player1, new NissaWhoShakesTheWorld());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("+1 puts counters on, untaps, and animates a noncreature land")
    void plusOneAnimatesLand() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        forest.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(forest.isTapped()).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("+1 may be activated without choosing a target")
    void plusOneMayChooseNoTarget() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("+1 can target only a noncreature land you control")
    void plusOneRestrictsTargets() {
        addReadyNissa(player1, 3);
        Permanent opponentForest = addLand(player2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentForest.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 creates an indestructible-land emblem and searches for any number of Forests")
    void minusEightCreatesEmblemAndFindsForests() {
        Permanent nissa = addReadyNissa(player1, 8);
        Permanent ownForest = addLand(player1);
        Permanent opponentForest = addLand(player2);
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        harness.setLibrary(player1, List.of(forest1, new PrimordialWurm(), forest2));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);
        assertThat(gqs.hasKeyword(gd, ownForest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentForest, Keyword.INDESTRUCTIBLE)).isFalse();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);
        assertThat(search.params().cards()).containsExactly(forest1, forest2);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> fetchedForests = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(CardType.LAND) && permanent != ownForest)
                .toList();
        assertThat(fetchedForests).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(fetchedForests).allMatch(permanent ->
                gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE));
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Primordial Wurm");
    }

    @Test
    void opponentsForestDoesNotProduceExtraMana() {
        harness.addToBattlefield(player1, new NissaWhoShakesTheWorld());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void nonForestDoesNotProduceExtraGreen() {
        harness.addToBattlefield(player1, new NissaWhoShakesTheWorld());
        harness.addToBattlefield(player1, new Plains());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void animatedForestRetainsItsManaAbility() {
        addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        forest.setSummoningSick(true);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    void plusOneCannotTargetAnAlreadyAnimatedLand() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();
        nissa.setLoyaltyActivationsThisTurn(0);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removedTargetDoesNotAnimateAnotherLand() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent target = addLand(player1);
        Permanent otherLand = addLand(player1);
        otherLand.tap();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(otherLand.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherLand.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, otherLand)).isFalse();
    }

    @Test
    void ultimateMayFindZeroForests() {
        addReadyNissa(player1, 8);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.emblems).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Nissa, Who Shakes the World");
    }

    @Test
    void ultimateMayStopAfterFindingOneForest() {
        addReadyNissa(player1, 8);
        Card forest1 = new Forest();
        Card forest2 = new Forest();
        harness.setLibrary(player1, List.of(forest1, forest2));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest2);
        List<Permanent> lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> gqs.isLand(gd, permanent)).toList();
        assertThat(lands).hasSize(1);
        assertThat(lands.getFirst().getCard()).isSameAs(forest1);
        assertThat(lands.getFirst().isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, lands.getFirst(), Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void animationRemainsAfterNissaLeavesTheBattlefield() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(nissa);
        gd.playerGraveyards.get(player1.getId()).add(nissa.getCard());

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NissaWhoShakesTheWorld());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
