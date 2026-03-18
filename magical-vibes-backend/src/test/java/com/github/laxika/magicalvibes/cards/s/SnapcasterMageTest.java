package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientGrudge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SnapcasterMageTest extends BaseCardTest {

    @Test
    @DisplayName("Has ON_ENTER_BATTLEFIELD GrantFlashbackToTargetGraveyardCardEffect for instants and sorceries")
    void hasCorrectETBEffect() {
        SnapcasterMage card = new SnapcasterMage();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(GrantFlashbackToTargetGraveyardCardEffect.class);
        GrantFlashbackToTargetGraveyardCardEffect effect =
                (GrantFlashbackToTargetGraveyardCardEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.cardTypes()).containsExactlyInAnyOrder(CardType.INSTANT, CardType.SORCERY);
    }

    @Test
    @DisplayName("ETB with instant in controller's graveyard prompts graveyard choice")
    void etbPromptsGraveyardChoice() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB only shows instant/sorcery cards from controller's graveyard")
    void etbOnlyShowsInstantSorceryFromController() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Set<UUID> validIds = gd.interaction.multiSelection().multiGraveyardValidCardIds();
        assertThat(validIds).hasSize(1);
        assertThat(validIds).contains(shock.getId());
    }

    @Test
    @DisplayName("ETB grants flashback to chosen graveyard card")
    void etbGrantsFlashbackToChosenCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Choose Shock as target
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Granted flashback allows casting the spell from graveyard")
    void grantedFlashbackAllowsCasting() {
        Shock shock = new Shock();
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        // Snapcaster costs {1}{U}, Shock costs {R}
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));
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
        Permanent creature = addReadyCreature(player2);

        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("ETB with no instant/sorcery in graveyard does not prompt")
    void etbWithNoValidTargetsDoesNotPrompt() {
        // Only creature in graveyard — no valid targets
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB with empty graveyard does not prompt")
    void etbWithEmptyGraveyardDoesNotPrompt() {
        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB does not target opponent's graveyard cards")
    void etbDoesNotTargetOpponentGraveyard() {
        // Put instant only in opponent's graveyard
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setGraveyard(player1, List.of());

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB fizzles when targeted card is removed from graveyard before resolution")
    void etbFizzlesWhenTargetRemoved() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Choose Shock
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));

        // Remove Shock from graveyard before ETB trigger resolves
        gd.playerGraveyards.get(player1.getId()).clear();

        // Resolve ETB trigger → should fizzle
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(shock.getId());
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Does not grant flashback to card that already has native flashback")
    void doesNotExcludeCardsWithNativeFlashback() {
        // Snapcaster Mage targets any instant/sorcery — per the rules, it can target
        // a card that already has flashback (it just won't change the flashback cost).
        // The card is still a valid target.
        AncientGrudge grudge = new AncientGrudge();
        harness.setGraveyard(player1, List.of(grudge));

        harness.setHand(player1, List.of(new SnapcasterMage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Cards with native flashback should still be targetable
        Set<UUID> validIds = gd.interaction.multiSelection().multiGraveyardValidCardIds();
        assertThat(validIds).contains(grudge.getId());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
