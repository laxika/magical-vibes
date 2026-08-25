package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowOfTheGoblin.class, Forest.class, GrizzlyBears.class})
class ShadowOfTheGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of the first main phase, discarding draws a card")
    void rummagesAtFirstMainPhase() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        Forest discarded = new Forest();
        Forest kept = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded, kept));
        harness.setLibrary(player1, List.of(drawn));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
    }

    @Test
    @DisplayName("The first-main ability does nothing when its controller has no card to discard")
    void noDiscardMeansNoDraw() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        Forest topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Playing a land from a graveyard deals damage to each opponent")
    void graveyardLandPlayTriggersDamage() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        harness.setLife(player2, 20);
        Forest land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        gd.graveyardPlayPermissions.put(land.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playGraveyardLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Casting a spell from exile deals damage to each opponent")
    void exileSpellCastTriggersDamage() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        harness.setLife(player2, 20);
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Playing a land from hand does not trigger the damage ability")
    void handLandPlayDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }
}
