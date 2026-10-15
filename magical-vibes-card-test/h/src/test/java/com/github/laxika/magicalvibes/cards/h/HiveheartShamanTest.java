package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TropicalIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiveheartShaman.class, Forest.class, Island.class, Mountain.class, Plains.class,
        Swamp.class, TropicalIsland.class, AshayaSoulOfTheWild.class})
class HiveheartShamanTest extends BaseCardTest {

    @Test
    void attackTriggerOffersBasicLandsWithNoSharedLandType() {
        addReadyShaman();
        harness.addToBattlefield(player1, new TropicalIsland());
        harness.setLibrary(player1, List.of(
                new Forest(), new Island(), new Mountain(), new Plains(), new Swamp()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Mountain", "Plains", "Swamp");

        harness.handleCardChosen(player1, 0);

        Permanent mountain = findPermanent(player1, "Mountain");
        assertThat(mountain.isTapped()).isFalse();
    }

    @Test
    void attackTriggerMayBeDeclined() {
        addReadyShaman();
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
    }

    @Test
    void activatedAbilityCreatesInsectWithDomainCounters() {
        addReadyShaman();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent insect = findPermanent(player1, "Insect");
        assertThat(insect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(insect.getEffectivePower()).isEqualTo(4);
        assertThat(insect.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void activatedAbilityRequiresSorcerySpeed() {
        addReadyShaman();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void attackTriggerIgnoresOpponentLandTypesAndExcludesNonbasicLands() {
        addReadyShaman();
        harness.addToBattlefield(player2, new Forest());
        harness.setLibrary(player1, List.of(new TropicalIsland(), new Forest(), new HiveheartShaman()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
        harness.handleCardChosen(player1, 0);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
    }

    @Test
    void attackTriggerCanFailToFindAnEligibleLand() {
        addReadyShaman();
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Mountain")).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
    }

    @Test
    void attackTriggerCompletesWhenNoEligibleLandIsInLibrary() {
        addReadyShaman();
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    @Test
    void activatedAbilityCreatesOneInsectWithZeroDomain() {
        harness.addToBattlefield(player1, new HiveheartShaman());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        Permanent insect = findPermanent(player1, "Insect");
        assertThat(insect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(insect.getEffectivePower()).isEqualTo(1);
        assertThat(insect.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void activatedAbilityCountsDistinctTypesOnNonbasicLandsAndIgnoresOpponentLands() {
        addReadyShaman();
        harness.addToBattlefield(player1, new TropicalIsland());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Swamp());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Insect").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void activatedAbilityCountsLandTypesWhenItResolvesEvenIfShamanLeaves() {
        addReadyShaman();
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(0);
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Insect").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void activatedAbilityCountsCreaturesMadeIntoForestLandsByAshaya() {
        addReadyShaman();
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent insect = findPermanent(player1, "Insect");
        assertThat(insect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(insect.getEffectivePower()).isEqualTo(2);
        assertThat(insect.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void activatedAbilityCannotBeActivatedDuringCombat() {
        addReadyShaman();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void activatedAbilityCannotBeActivatedWithAnAbilityOnTheStack() {
        addReadyShaman();
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    private void addReadyShaman() {
        addCreatureReady(player1, new HiveheartShaman());
    }
}
