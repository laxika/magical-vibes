package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasilicaStalker.class, GrizzlyBears.class})
class BasilicaStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player gains 1 life and surveils 1")
    void combatDamageGainsLifeAndSurveils() {
        Permanent stalker = addCreatureReady(player1, new BasilicaStalker());
        Card topCard = new GrizzlyBears();
        Card keptCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, keptCard));
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(stalker)));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Disguise casts Basilica Stalker face down")
    void disguiseCastsFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BasilicaStalker()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Basilica Stalker").isFaceDown()).isTrue();
    }
}
