package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AjaniSleeperAgent.class, GrizzlyBears.class, ChandraNalaar.class, LightningBolt.class,
        AutomaticLibrarian.class})
class AjaniSleeperAgentTest extends BaseCardTest {

    @Test
    void plusOnePutsCreatureIntoHand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOnePutsPlaneswalkerIntoHand() {
        harness.setLibrary(player1, List.of(new ChandraNalaar()));
        addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof ChandraNalaar);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOneCanPutNonmatchingCardOnBottom() {
        GrizzlyBears cardBelow = new GrizzlyBears();
        LightningBolt topCard = new LightningBolt();
        harness.setLibrary(player1, List.of(topCard, cardBelow));
        addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cardBelow, topCard);
    }

    @Test
    void minusThreeDistributesCountersAndGrantsVigilance() {
        addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.ensurePriority(player1);
        harness.getGameService().activateAbility(
                gd,
                player1,
                0,
                1,
                null,
                null,
                null,
                List.of(first.getId(), second.getId(), third.getId()),
                Map.of(first.getId(), 1, second.getId(), 1, third.getId(), 1)
        );
        harness.passBothPriorities();

        assertThat(first.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(second.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(third.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, third, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void emblemPoisonsOpponentWhenCreatureIsCast() {
        addReadyAjani(6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getDescription().contains("Ajani, Sleeper Agent's emblem"));
        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void emblemDoesNotTriggerForNoncreatureSpell() {
        addReadyAjani(6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Ajani, Sleeper Agent's emblem"));
    }

    @Test
    void plusOneCanLeaveNonmatchingCardOnTop() {
        LightningBolt topCard = new LightningBolt();
        AutomaticLibrarian cardBelow = new AutomaticLibrarian();
        harness.setLibrary(player1, List.of(topCard, cardBelow));
        Permanent ajani = addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, cardBelow);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void plusOneWithEmptyLibraryStillPaysLoyaltyCost() {
        harness.setLibrary(player1, List.of());
        Permanent ajani = addReadyAjani(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void entersWithFullLoyaltyWhenHybridSymbolIsPaidWithGreenMana() {
        harness.setHand(player1, List.of(new AjaniSleeperAgent()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ajani, Sleeper Agent").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(4);
        harness.assertLife(player1, 20);
    }

    @Test
    void entersWithFullLoyaltyWhenHybridSymbolIsPaidWithWhiteMana() {
        harness.setHand(player1, List.of(new AjaniSleeperAgent()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ajani, Sleeper Agent").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(4);
        harness.assertLife(player1, 20);
    }

    @Test
    void compleatedReducesLoyaltyWhenHybridSymbolIsPaidWithLife() {
        harness.setHand(player1, List.of(new AjaniSleeperAgent()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ajani, Sleeper Agent").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void minusThreeCanChooseNoTargets() {
        Permanent ajani = addReadyAjani(4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusThreeCanPutAllCountersOnOpponentsCreature() {
        Permanent ajani = addReadyAjani(4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AutomaticLibrarian());

        harness.ensurePriority(player1);
        gs.activateAbility(gd, player1, 0, 1, null, null, null,
                List.of(target.getId()), Map.of(target.getId(), 3));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void minusThreeRejectsIncompleteDistributionWithoutPayingLoyalty() {
        Permanent ajani = addReadyAjani(4);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AutomaticLibrarian());

        harness.ensurePriority(player1);
        assertThatThrownBy(() -> gs.activateAbility(gd, player1, 0, 1, null, null, null,
                List.of(target.getId()), Map.of(target.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusThreeDoesNotRedistributeCountersFromRemovedTarget() {
        addReadyAjani(4);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AutomaticLibrarian());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AutomaticLibrarian());
        harness.ensurePriority(player1);
        gs.activateAbility(gd, player1, 0, 1, null, null, null,
                List.of(first.getId(), second.getId()), Map.of(first.getId(), 2, second.getId(), 1));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void emblemTriggersForPlaneswalkerAfterAjaniLeavesBattlefield() {
        addReadyAjani(6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ajani, Sleeper Agent");
        harness.setHand(player1, List.of(new AjaniSleeperAgent()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castPlaneswalker(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Ajani, Sleeper Agent");
    }

    @Test
    void emblemDoesNotTriggerForOpponentsCreatureSpell() {
        addReadyAjani(6);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AutomaticLibrarian()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyAjani(int loyalty) {
        Permanent ajani = harness.addToBattlefieldAndReturn(player1, new AjaniSleeperAgent());
        ajani.setCounterCount(CounterType.LOYALTY, loyalty);
        ajani.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ajani;
    }
}
