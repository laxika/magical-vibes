package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HangarbackWalker;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmarthisGhostfireInitiate.class, BurstOfStrength.class, PhyrexianWalker.class,
        GrizzlyBears.class, LightningBolt.class, HangarbackWalker.class})
class OmarthisGhostfireInitiateTest extends BaseCardTest {

    @Test
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new OmarthisGhostfireInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Omarthis, Ghostfire Initiate")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void triggersOnlyForColorlessCreaturesAndMayPutACounterOnItself() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent colorlessCreature = addCreatureReady(player1, new PhyrexianWalker());
        Permanent coloredCreature = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(coloredCreature);
        resolveAllTriggers();
        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        putCounterOn(colorlessCreature);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void diesAndManifestsOneCardForEachCounter() {
        Card first = new GrizzlyBears();
        Card second = new PhyrexianWalker();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));

        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, omarthis.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void triggersOnceWhenAnotherColorlessCreatureEntersWithMultipleCounters() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new HangarbackWalker()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0, 3);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayDeclineTheCounter() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent walker = addCreatureReady(player1, new PhyrexianWalker());

        putCounterOn(walker);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void puttingCountersOnItselfDoesNotTriggerAgain() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        putCounterOn(omarthis);
        resolveAllTriggers();

        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggersWhenItsControllerPutsCountersOnAnOpponentsColorlessCreature() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent walker = addCreatureReady(player2, new PhyrexianWalker());

        putCounterOn(walker);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenAnOpponentPutsCountersOnItsControllersCreature() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent walker = addCreatureReady(player1, new PhyrexianWalker());
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player2, 0, walker.getId());
        resolveAllTriggers();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingWithZeroXDiesWithoutManifesting() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new OmarthisGhostfireInitiate()));

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Omarthis, Ghostfire Initiate");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void deathCountsOtherCounterTypesAndManifestsNoncreatureCardsToo() {
        Card first = new LightningBolt();
        Card second = new BurstOfStrength();
        Card third = new GrizzlyBears();
        Card fourth = new PhyrexianWalker();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        omarthis.setCounterCount(CounterType.STUN, 2);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, omarthis.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Omarthis, Ghostfire Initiate");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3)
                .allMatch(Permanent::isManifested)
                .allMatch(Permanent::isFaceDown)
                .extracting(Permanent::getCard).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
    }

    @Test
    void manifestsOnlyAvailableCardsWhenLibraryIsTooShort() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, omarthis.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(Permanent::isManifested)
                .extracting(Permanent::getCard).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void manifestedOmarthisHasNoCounterTriggeredAbilityWhileFaceDown() {
        OmarthisGhostfireInitiate libraryCard = new OmarthisGhostfireInitiate();
        harness.setLibrary(player1, List.of(libraryCard));
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent walker = addCreatureReady(player1, new PhyrexianWalker());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, omarthis.getId());
        resolveAllTriggers();
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard()).isSameAs(libraryCard);
        assertThat(manifested.isFaceDown()).isTrue();

        putCounterOn(walker);
        resolveAllTriggers();

        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTriggerAgainForASeparatePlacementWithoutTriggeringOnItsOwnAddedCounter() {
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent walker = addCreatureReady(player1, new PhyrexianWalker());

        for (int i = 0; i < 2; i++) {
            putCounterOn(walker);
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.stack).isEmpty();
        }

        assertThat(omarthis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(walker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void deathWithAnEmptyLibraryDoesNotCauseALoss() {
        harness.setLibrary(player1, List.of());
        Permanent omarthis = addCreatureReady(player1, new OmarthisGhostfireInitiate());
        omarthis.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, omarthis.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Omarthis, Ghostfire Initiate");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    private void putCounterOn(Permanent target) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
