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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
