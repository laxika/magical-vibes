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

    @Test
    @DisplayName("Casting a spell from hand does not trigger damage")
    void handSpellCastDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ShadowOfTheGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent casting from exile does not trigger damage")
    void opponentExileSpellDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        ShadowOfTheGoblin spell = new ShadowOfTheGoblin();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromExile(player2, spell.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The rummage ability does not trigger in an opponent's first main phase")
    void opponentFirstMainPhaseDoesNotRummage() {
        harness.addToBattlefield(player1, new ShadowOfTheGoblin());
        Forest held = new Forest();
        Forest topCard = new Forest();
        harness.setHand(player1, List.of(held));
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(held);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
