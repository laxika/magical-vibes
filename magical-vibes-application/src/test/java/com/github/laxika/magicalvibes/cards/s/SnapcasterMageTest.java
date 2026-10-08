package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.cards.c.Conflagrate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnapcasterMage.class, Shock.class, GrizzlyBears.class, AncientGrudge.class, BumpInTheNight.class, Conflagrate.class})
class SnapcasterMageTest extends BaseCardTest {

    

    @Test
    @DisplayName("ETB with instant in controller's graveyard prompts graveyard choice")
    void etbPromptsGraveyardChoice() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("ETB only shows instant/sorcery cards from controller's graveyard")
    void etbOnlyShowsInstantSorceryFromController() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));

        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();

        List<UUID> validIds = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).hasSize(1);
        assertThat(validIds).contains(shock.getId());
    }

    @Test
    @DisplayName("ETB grants flashback to chosen graveyard card")
    void etbGrantsFlashbackToChosenCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Choose Shock as target
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Granted flashback allows casting the spell from graveyard")
    void grantedFlashbackAllowsCasting() {
        Shock shock = new Shock();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setSummoningSick(false);

        harness.setGraveyard(player1, List.of(shock));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger → grant flashback

        // Now cast Shock from graveyard with granted flashback
        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Card cast with granted flashback is exiled after resolution")
    void grantedFlashbackExilesAfterResolution() {
        Shock shock = new Shock();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setSummoningSick(false);

        harness.setGraveyard(player1, List.of(shock));

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("ETB with no instant/sorcery in graveyard does not prompt")
    void etbWithNoValidTargetsDoesNotPrompt() {
        // Only creature in graveyard — no valid targets
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB with empty graveyard does not prompt")
    void etbWithEmptyGraveyardDoesNotPrompt() {
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB does not target opponent's graveyard cards")
    void etbDoesNotTargetOpponentGraveyard() {
        // Put instant only in opponent's graveyard
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setGraveyard(player1, List.of());

        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB fizzles when targeted card is removed from graveyard before resolution")
    void etbFizzlesWhenTargetRemoved() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();

        // Choose Shock
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        // Remove Shock from graveyard before ETB trigger resolves
        gd.playerGraveyards.get(player1.getId()).clear();

        // Resolve ETB trigger → should fizzle
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(shock.getId());
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cards with native flashback remain valid targets")
    void doesNotExcludeCardsWithNativeFlashback() {
        AncientGrudge grudge = new AncientGrudge();
        harness.setGraveyard(player1, List.of(grudge));

        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();

        // Cards with native flashback should still be targetable
        List<UUID> validIds = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).contains(grudge.getId());
    }

    @Test
    void grantsSorceryFlashbackAtItsManaCostDespiteHigherNativeCost() {
        BumpInTheNight bump = new BumpInTheNight();
        harness.setGraveyard(player1, List.of(bump));
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bump.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bump);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @CardUsed(Conflagrate.class)
    void grantedFlashbackDoesNotRequireNativeFlashbackDiscards() {
        Conflagrate conflagrate = new Conflagrate();
        harness.setGraveyard(player1, List.of(conflagrate));
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(conflagrate.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 5);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castFlashbackForXWithDiscards(
                player1, 0, 2, Map.of(player2.getId(), 2), List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(conflagrate);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void grantedFlashbackExpiresAfterTheTurn() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
    }

    @Test
    void flashAllowsCastingDuringOpponentsUpkeep() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Snapcaster Mage");
        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void grantedFlashbackDoesNotAllowSorceriesDuringCombat() {
        BumpInTheNight bump = new BumpInTheNight();
        harness.setGraveyard(player1, List.of(bump));
        harness.castFromHand(player1, new SnapcasterMage(), "{1}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bump.getId()));
        harness.passBothPriorities();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bump in the Night");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(bump);
    }

}
