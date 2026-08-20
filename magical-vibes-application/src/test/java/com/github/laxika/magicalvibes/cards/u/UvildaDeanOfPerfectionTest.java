package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UvildaDeanOfPerfectionTest extends BaseCardTest {

    @Test
    void exilesAnInstantFromHandWithThreeRefineCounters() {
        addReadyUvilda();
        DarkRitual ritual = new DarkRitual();
        harness.setHand(player1, List.of(new GrizzlyBears(), ritual));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandWithRefineCountersChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.exiledCardRefineCounters).containsEntry(ritual.getId(), 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    void lastRefineCounterOffersTheSpellForFourLessAndCastsIt() {
        addReadyUvilda();
        DarkRitual ritual = new DarkRitual();
        harness.setExile(player1, List.of(ritual));
        gd.exiledCardRefineCounters.put(ritual.getId(), 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        triggerUpkeep(player1);

        assertThat(gd.exiledCardRefineCounters).doesNotContainKey(ritual.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(ritual.getId())).isNull();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(ritual.getId()));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    void nassariExilesEachOpponentsTopSpellAndCountsTheCast() {
        UvildaDeanOfPerfection card = new UvildaDeanOfPerfection();
        Permanent nassari = harness.addToBattlefieldAndReturn(player1, card);
        nassari.setCard(card.getBackFaceCard());
        nassari.setTransformed(true);
        nassari.setSummoningSick(false);
        harness.setLibrary(player2, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        triggerUpkeep(player1);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c instanceof DarkRitual)
                .findFirst()
                .orElseThrow();
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player1.getId());
        assertThat(gd.exilePlayAnyManaType).contains(exiled.getId());

        harness.castFromExile(player1, exiled.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(nassari.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyUvilda() {
        Permanent uvilda = harness.addToBattlefieldAndReturn(player1, new UvildaDeanOfPerfection());
        uvilda.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return uvilda;
    }

    private void triggerUpkeep(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
