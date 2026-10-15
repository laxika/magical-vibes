package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeldonsCane;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DutifulKnowledgeSeeker.class, Forest.class, GrizzlyBears.class, FeldonsCane.class})
class DutifulKnowledgeSeekerTest extends BaseCardTest {

    @Test
    void puttingCardIntoLibraryAddsCounter() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new DutifulKnowledgeSeeker());
        Card target = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(seeker), 0,
                target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId(), target.getId());
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canPutAnOpponentsGraveyardCardIntoItsOwnersLibrary() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new DutifulKnowledgeSeeker());
        Card target = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(seeker), 0,
                target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId(), target.getId());
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void shufflingMultipleGraveyardCardsIntoLibraryTriggersOnceForEachSeeker() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new DutifulKnowledgeSeeker());
        Permanent opposingSeeker = harness.addToBattlefieldAndReturn(player2, new DutifulKnowledgeSeeker());
        harness.addToBattlefield(player1, new FeldonsCane());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingSeeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void shufflingLibraryWithoutMovingCardsDoesNotAddCounter() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new DutifulKnowledgeSeeker());
        harness.addToBattlefield(player1, new FeldonsCane());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void secondActivationCanResolveBeforeTheFirstAndMakeItsTargetIllegal() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new DutifulKnowledgeSeeker());
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(seeker.isTapped()).isFalse();
    }
}
