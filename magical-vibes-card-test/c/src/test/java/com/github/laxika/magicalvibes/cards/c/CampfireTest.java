package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BruseTarlBoorishHerder;
import com.github.laxika.magicalvibes.cards.t.TymnaTheWeaver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Campfire.class, TymnaTheWeaver.class, BruseTarlBoorishHerder.class})
class CampfireTest extends BaseCardTest {

    @Test
    void gainsTwoLife() {
        harness.addToBattlefield(player1, new Campfire());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void returnsAllCommandersFromCommandZoneAndGraveyardThenShufflesRemainingGraveyard() {
        Campfire campfire = new Campfire();
        harness.addToBattlefield(player1, campfire);

        Card commandZoneCommander = new TymnaTheWeaver();
        Card graveyardCommander = new BruseTarlBoorishHerder();
        Card remainingGraveyardCard = new Campfire();
        gd.makeCommander(player1.getId(), commandZoneCommander);
        gd.playerCommanders.get(player1.getId()).add(graveyardCommander);
        gd.playerCommandZones.get(player1.getId()).add(commandZoneCommander);
        harness.setGraveyard(player1, List.of(graveyardCommander, remainingGraveyardCard));

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderReplacementChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderReplacementChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(commandZoneCommander, graveyardCommander);
        assertThat(gd.playerCommandZones.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(remainingGraveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getCard() == campfire);
    }

    @Test
    void canKeepGraveyardCommanderInCommandZoneInsteadOfPuttingItIntoHand() {
        harness.addToBattlefield(player1, new Campfire());
        Card commander = new TymnaTheWeaver();
        gd.makeCommander(player1.getId(), commander);
        harness.setGraveyard(player1, List.of(commander));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderReplacementChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerCommandZones.get(player1.getId())).contains(commander);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(commander);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void canLeaveCommandZoneCommanderThereAndDoesNotMoveOpponentsCommander() {
        harness.addToBattlefield(player1, new Campfire());
        Card commander = new TymnaTheWeaver();
        Card opponentCommander = new BruseTarlBoorishHerder();
        gd.makeCommander(player1.getId(), commander);
        gd.makeCommander(player2.getId(), opponentCommander);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        gd.playerCommandZones.get(player2.getId()).add(opponentCommander);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderReplacementChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(commander);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(commander, opponentCommander);
        assertThat(gd.playerCommandZones.get(player2.getId())).containsExactly(opponentCommander);
    }

    @Test
    void exilesCampfireAsACostAndShufflesWithoutAnyCommanders() {
        Campfire campfire = new Campfire();
        Card graveyardCard = new Campfire();
        Card opponentGraveyardCard = new Campfire();
        harness.addToBattlefield(player1, campfire);
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Campfire");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == campfire);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(deckSizeBefore + 1).contains(graveyardCard).doesNotContain(campfire);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
    }

    @Test
    void lifeAbilityTapsCampfireAndCannotBeActivatedAgainWhileTapped() {
        Permanent campfire = harness.addToBattlefieldAndReturn(player1, new Campfire());
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(campfire.isTapped()).isTrue();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 15);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
