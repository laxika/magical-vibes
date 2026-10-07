package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ParanormalAnalyst;
import com.github.laxika.magicalvibes.cards.f.FearOfLostTeeth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderwaterTunnelSlimyAquarium.class, FearOfLostTeeth.class, Forest.class, ParanormalAnalyst.class})
class UnderwaterTunnelSlimyAquariumTest extends BaseCardTest {

    @Test
    void unlockingUnderwaterTunnelSurveilsTwo() {
        Card firstCard = new FearOfLostTeeth();
        Card secondCard = new Forest();
        harness.setHand(player1, List.of(new UnderwaterTunnelSlimyAquarium()));
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        resolveAllTriggers();

        Permanent room = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(room.isRoomDoorUnlocked(0)).isTrue();
        assertThat(room.isRoomDoorUnlocked(1)).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstCard, secondCard);
    }

    @Test
    void unlockingSlimyAquariumManifestsDreadAndPutsCounterOnThatCreature() {
        Card manifestedCard = new FearOfLostTeeth();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnderwaterTunnelSlimyAquarium()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }

    @Test
    void surveilCanKeepBothCardsInReverseOrder() {
        Card firstCard = new Forest();
        Card secondCard = new UnderwaterTunnelSlimyAquarium();
        Card thirdCard = new Forest();
        harness.setHand(player1, List.of(new UnderwaterTunnelSlimyAquarium()));
        harness.setLibrary(player1, List.of(firstCard, secondCard, thirdCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard, thirdCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void unlockingSecondDoorManifestsANoncreatureAndCountersOnlyIt() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new UnderwaterTunnelSlimyAquarium();
        harness.setHand(player1, List.of(new UnderwaterTunnelSlimyAquarium()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castModalSorcery(player1, 0, 0, List.of());
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        Permanent room = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.unlockRoomDoor(player1, 0, 1);
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getOriginalCard()).isSameAs(manifestedCard);
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(room.isRoomFullyUnlocked()).isTrue();
        assertThat(room.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestDreadWithOneCardStillPutsCounterOnManifestedCreature() {
        Card onlyCard = new Forest();
        harness.setHand(player1, List.of(new UnderwaterTunnelSlimyAquarium()));
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getOriginalCard()).isSameAs(onlyCard);
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestDreadWithEmptyLibraryDoesNotCounterAnotherCreature() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new FearOfLostTeeth());
        harness.setHand(player1, List.of(new UnderwaterTunnelSlimyAquarium()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void manifestDreadWithEmptyLibraryStillTriggersParanormalAnalyst() {
        Permanent analyst = harness.addToBattlefieldAndReturn(player1, new ParanormalAnalyst());
        harness.setHand(player1, List.of(new UnderwaterTunnelSlimyAquarium()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(analyst.getId());
    }
}
