package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinersGuidewing.class, Forest.class})
class MinersGuidewingTest extends BaseCardTest {

    @Test
    @DisplayName("When Miner's Guidewing dies, target creature you control explores a land")
    void deathTriggerExploresLand() {
        Permanent guidewing = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guidewing));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(land.getId());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("When Miner's Guidewing dies, target creature you control explores a nonland")
    void deathTriggerExploresNonland() {
        Permanent guidewing = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Card nonland = new MinersGuidewing();
        harness.setLibrary(player1, List.of(nonland));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guidewing));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Miners Guidewing cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent guidewing = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        harness.addToBattlefield(player2, new MinersGuidewing());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guidewing));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("no valid targets"));
    }

    @Test
    void nonlandCanRemainOnTop() {
        Permanent guidewing = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Card revealed = new MinersGuidewing();
        Card next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guidewing));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed, next);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(revealed);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonlandCanGoToGraveyard() {
        Permanent guidewing = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Card revealed = new MinersGuidewing();
        Card next = new Forest();
        harness.setLibrary(player1, List.of(revealed, next));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guidewing));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyLibraryGivesCounter() {
        Permanent guidewing = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        harness.setLibrary(player1, List.of());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guidewing));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removedTargetDoesNotExplore() {
        Permanent guidewing = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MinersGuidewing());
        Card revealed = new Forest();
        harness.setLibrary(player1, List.of(revealed));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, guidewing));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.stack).isEmpty();
    }
}
