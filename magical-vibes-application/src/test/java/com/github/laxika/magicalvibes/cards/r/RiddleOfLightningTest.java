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
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
