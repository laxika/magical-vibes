package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DragonKamisEgg;
import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDragonKamiReborn.class, DragonKamisEgg.class, DragonWhelp.class,
        Forest.class, GrizzlyBears.class, Opt.class})
class TheDragonKamiRebornTest extends BaseCardTest {

    @Test
    void chapterIExilesOneCardWithHatchingCounterAndBottomsTheRest() {
        Card first = new GrizzlyBears();
        Card chosen = new Forest();
        Card third = new Opt();
        harness.setLibrary(player1, List.of(first, chosen, third));
        Permanent saga = addSagaWithLore(0);

        triggerChapter();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(first, chosen, third);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.exiledCardsWithHatchingCounters).containsExactly(chosen.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first);
        assertThat(saga.isTransformed()).isFalse();
    }

    @Test
    void chapterIIITransformsIntoDragonKamisEgg() {
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();

        Permanent egg = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DragonKamisEgg)
                .findFirst()
                .orElseThrow();
        assertThat(egg.isTransformed()).isTrue();
    }

    @Test
    void eggDeathOffersOwnedHatchingCreatureForFree() {
        Permanent egg = addTransformedEgg();
        Card creature = new GrizzlyBears();
        Card noncreature = new Forest();
        Card opponentCreature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature, noncreature));
        harness.setExile(player2, List.of(opponentCreature));
        gd.exiledCardsWithHatchingCounters.add(creature.getId());
        gd.exiledCardsWithHatchingCounters.add(noncreature.getId());
        gd.exiledCardsWithHatchingCounters.add(opponentCreature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(creature.getId())).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.findExiledCard(opponentCreature.getId())).isNotNull();
    }

    @Test
    void anotherDragonYouControlAlsoTriggersTheEgg() {
        addTransformedEgg();
        Permanent dragon = addCreatureReady(player1, new DragonWhelp());
        Card creature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature));
        gd.exiledCardsWithHatchingCounters.add(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void chapterITriggersWhenTheSagaEnters() {
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));

        harness.castFromHand(player1, new TheDragonKamiReborn(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.findExiledCard(card.getId()).faceDown()).isTrue();
        assertThat(gd.exiledCardsWithHatchingCounters).contains(card.getId());
    }

    @Test
    void chapterIIExilesTheOnlyRemainingCardAndStillGainsLife() {
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        addSagaWithLore(1);

        triggerChapter();

        harness.assertLife(player1, 22);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId()).faceDown()).isTrue();
        assertThat(gd.exiledCardsWithHatchingCounters).containsExactly(card.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotPreventChapterLifeGain() {
        harness.setLibrary(player1, List.of());
        addSagaWithLore(0);

        triggerChapter();

        harness.assertLife(player1, 22);
        assertThat(gd.exiledCardsWithHatchingCounters).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void twoCardLibraryExilesOneAndBottomsTheOther() {
        Card chosen = new Forest();
        Card remaining = new Forest();
        harness.setLibrary(player1, List.of(chosen, remaining));
        addSagaWithLore(0);

        triggerChapter();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 22);
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.exiledCardsWithHatchingCounters).containsExactly(chosen.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void exilingPlayerCanStillLookAtTheCardAfterTheSagaLeaves() {
        Card card = new Forest();
        harness.setLibrary(player1, List.of(card));
        Permanent saga = addSagaWithLore(0);
        triggerChapter();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, saga));
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage controllerState = mapper.readValue(harness.getConn1()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        GameStateMessage opponentState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);

        assertThat(opponentState.lookedAtExileCards()).noneMatch(view -> view.id().equals(card.getId()));
        assertThat(controllerState.lookedAtExileCards()).anyMatch(view -> view.id().equals(card.getId()));
    }

    @Test
    void oneDeathAllowsOnlyOneCreatureSpellFromMultipleEligibleCards() {
        Permanent egg = addTransformedEgg();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.inMutationScope(() -> {
            gd.addToExile(player1.getId(), first, null, true);
            gd.addToExile(player1.getId(), second, null, true);
        });
        gd.exiledCardsWithHatchingCounters.add(first.getId());
        gd.exiledCardsWithHatchingCounters.add(second.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.exiledCardsWithHatchingCounters).doesNotContain(first.getId());
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    void decliningTheFirstCreatureStillAllowsChoosingAnother() {
        Permanent egg = addTransformedEgg();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setExile(player1, List.of(first, second));
        gd.exiledCardsWithHatchingCounters.add(first.getId());
        gd.exiledCardsWithHatchingCounters.add(second.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(second.getId());
    }

    @Test
    void decliningTheCastLeavesTheCardAndHatchingCounterInExile() {
        Permanent egg = addTransformedEgg();
        Card creature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature));
        gd.exiledCardsWithHatchingCounters.add(creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, egg));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.exiledCardsWithHatchingCounters).contains(creature.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anotherNonDragonDyingDoesNotTriggerTheEgg() {
        addTransformedEgg();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature));
        gd.exiledCardsWithHatchingCounters.add(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bear));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void opponentsDragonDyingDoesNotTriggerTheEgg() {
        addTransformedEgg();
        Permanent dragon = addCreatureReady(player2, new DragonWhelp());
        Card creature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature));
        gd.exiledCardsWithHatchingCounters.add(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dragon));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    @Test
    void creatureWithoutAHatchingCounterCannotBeCast() {
        Permanent egg = addTransformedEgg();
        Card creature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, egg));

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void simultaneousEggAndDragonDeathsEachTrigger() {
        Permanent egg = addTransformedEgg();
        Permanent dragon = addCreatureReady(player1, new DragonWhelp());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setExile(player1, List.of(first, second));
        gd.exiledCardsWithHatchingCounters.add(first.getId());
        gd.exiledCardsWithHatchingCounters.add(second.getId());
        egg.setMarkedDamage(1);
        dragon.setMarkedDamage(3);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
    }

    @Test
    void exilingTheEggDoesNotTriggerItsDeathAbility() {
        Permanent egg = addTransformedEgg();
        Card creature = new GrizzlyBears();
        harness.setExile(player1, List.of(creature));
        gd.exiledCardsWithHatchingCounters.add(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, egg));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheDragonKamiReborn());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private Permanent addTransformedEgg() {
        TheDragonKamiReborn card = new TheDragonKamiReborn();
        Permanent egg = harness.addToBattlefieldAndReturn(player1, card);
        egg.setCard(card.getBackFaceCard());
        egg.setTransformed(true);
        return egg;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
