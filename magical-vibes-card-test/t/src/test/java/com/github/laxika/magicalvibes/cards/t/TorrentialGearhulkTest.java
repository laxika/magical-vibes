package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IncendiarySabotage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TorrentialGearhulk.class, Shock.class, CounselOfTheSoratami.class,
        GrizzlyBears.class, Cancel.class, IncendiarySabotage.class})
class TorrentialGearhulkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets only an instant from your graveyard")
    void etbTargetsOnlyAnInstantFromYourGraveyard() {
        Card instant = new Shock();
        Card sorcery = new CounselOfTheSoratami();
        Card opponentInstant = new Shock();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setGraveyard(player2, List.of(opponentInstant));
        harness.setHand(player1, List.of(new TorrentialGearhulk()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(instant.getId());
    }

    @Test
    @DisplayName("Casts the chosen instant for free and exiles it after resolution")
    void castsInstantForFreeAndExilesIt() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TorrentialGearhulk()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    void mayDeclineCastingAndLeaveInstantInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new TorrentialGearhulk()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void missingGraveyardTargetDoesNotOfferCasting() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new TorrentialGearhulk()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Torrential Gearhulk");
    }

    @Test
    void canCastCounterspellTargetingSpellAlreadyOnStack() {
        Cancel cancel = new Cancel();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(cancel));
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new TorrentialGearhulk()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(cancel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shock.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(cancel.getId()));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(cancel.getId()));
    }

    @Test
    void cannotPutSpellOnStackWithoutPayingMandatoryArtifactSacrifice() {
        IncendiarySabotage sabotage = new IncendiarySabotage();
        harness.setGraveyard(player1, List.of(sabotage));
        harness.setHand(player1, List.of(new TorrentialGearhulk()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sabotage.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        boolean sabotageOnStack = gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getId().equals(sabotage.getId()));
        boolean gearhulkOnBattlefield = gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard() instanceof TorrentialGearhulk);
        assertThat(sabotageOnStack && gearhulkOnBattlefield).isFalse();
    }
}
