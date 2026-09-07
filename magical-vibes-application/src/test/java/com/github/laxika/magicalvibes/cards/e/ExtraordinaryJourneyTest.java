package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtraordinaryJourney.class, Forest.class, GrizzlyBears.class})
class ExtraordinaryJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to X target creatures and grants their owners permission to play them")
    void exilesUpToXTargetCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ExtraordinaryJourney()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player1, 0, 2, null, null, List.of(), List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(first.getOriginalCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getOriginalCard().getId(), player2.getId())
                .containsEntry(second.getOriginalCard().getId(), player2.getId());
    }

    @Test
    @DisplayName("The exiled creatures can be played by their owners without an added cost")
    void ownerCanPlayExiledCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ExtraordinaryJourney()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gs.playCard(gd, player1, 0, 1, null, null, List.of(), List.of());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castFromExile(player2, bear.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not target a land")
    void doesNotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new ExtraordinaryJourney()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null,
                List.of(forest.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Draws only once each turn for nontoken creatures entering from exile")
    void drawsOnlyOnceEachTurnForCreaturesEnteringFromExile() {
        harness.addToBattlefield(player1, new ExtraordinaryJourney());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        enterFromExile(player1, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        enterFromExile(player2, new GrizzlyBears());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        enterFromExile(player1, tokenCard);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws when a nontoken creature is cast from exile")
    void drawsWhenCreatureIsCastFromExile() {
        harness.addToBattlefield(player1, new ExtraordinaryJourney());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        Card bear = new GrizzlyBears();
        harness.setExile(player1, List.of(bear));
        gd.exilePlayPermissions.put(bear.getId(), player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent enterFromExile(com.github.laxika.magicalvibes.model.Player player, Card card) {
        harness.setExile(player, List.of(card));
        gd.removeFromExile(card.getId());
        Permanent permanent = new Permanent(card, Zone.EXILE);
        harness.inMutationScope(() -> {
            harness.getBattlefieldEntryService().putPermanentOntoBattlefield(
                    gd, player.getId(), permanent);
            harness.getBattlefieldEntryService().processCreatureETBEffects(
                    gd, player.getId(), card, null, false);
        });
        return permanent;
    }
}
