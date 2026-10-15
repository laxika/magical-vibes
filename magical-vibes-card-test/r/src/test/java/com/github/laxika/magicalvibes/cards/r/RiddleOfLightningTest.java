package com.github.laxika.magicalvibes.cards.r;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.f.FomoriNomad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({RiddleOfLightning.class, FomoriNomad.class, RiverOfTears.class})
class RiddleOfLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Scries before revealing the top card and damages the target by its mana value")
    void scriesThenDamagesTargetPlayerByRevealedManaValue() {
        Card topCard = new FomoriNomad();
        harness.setLibrary(player1,
                List.of(new RiverOfTears(), new RiverOfTears(), new RiverOfTears(), topCard));
        harness.setLife(player2, 20);

        castRiddle(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Deals the revealed card's mana value to a creature target")
    void damagesCreatureTarget() {
        harness.addToBattlefield(player2, new FomoriNomad());
        Card topCard = new FomoriNomad();
        harness.setLibrary(player1, List.of(topCard));

        castRiddle(harness.getPermanentId(player2, "Fomori Nomad"));
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Fomori Nomad");
        harness.assertInGraveyard(player2, "Fomori Nomad");
    }

    @Test
    @DisplayName("A zero-mana-value revealed card deals no damage")
    void landDealsNoDamage() {
        harness.setLibrary(player1, List.of(new RiverOfTears()));
        harness.setLife(player2, 20);

        castRiddle(player2.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An empty library reveals no card and deals no damage")
    void emptyLibraryDealsNoDamage() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);

        castRiddle(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void castRiddle(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RiddleOfLightning()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }

    @Test
    @DisplayName("Reordering scry cards changes which card is revealed without drawing it")
    void reorderedTopCardDeterminesDamage() {
        Card land = new RiverOfTears();
        Card creature = new FomoriNomad();
        Card otherLand = new RiverOfTears();
        harness.setLibrary(player1, List.of(land, creature, otherLand));
        harness.setLife(player2, 20);

        castRiddle(player2.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of(2)));

        harness.assertLife(player2, 15);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, land, otherLand);
        harness.assertInGraveyard(player1, "Riddle of Lightning");
    }

    @Test
    @DisplayName("Can target its controller")
    void damagesController() {
        harness.setLibrary(player1, List.of(new FomoriNomad()));
        harness.setLife(player1, 20);

        castRiddle(player1.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Does not scry when its only target leaves before resolution")
    void illegalTargetPreventsScry() {
        Card topCard = new FomoriNomad();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player2, new FomoriNomad());
        UUID targetId = harness.getPermanentId(player2, "Fomori Nomad");
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RiddleOfLightning()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Riddle of Lightning");
    }
}
