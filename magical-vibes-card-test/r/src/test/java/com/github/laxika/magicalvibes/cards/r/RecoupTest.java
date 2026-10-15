package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Recoup.class, Concentrate.class, AetherBurst.class, AvenFisher.class})
class RecoupTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants flashback to target sorcery in graveyard")
    void grantsFlashbackToTargetSorcery() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        harness.castAndResolveSorcery(player1, 0, concentrate.getId());

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(concentrate.getId());
    }

    @Test
    @DisplayName("Cannot target an instant or creature card in graveyard")
    void cannotTargetNonSorceryCard() {
        AetherBurst aetherBurst = new AetherBurst();
        AvenFisher avenFisher = new AvenFisher();
        harness.setGraveyard(player1, List.of(aetherBurst, avenFisher));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, aetherBurst.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, avenFisher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a sorcery card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player2, List.of(concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, concentrate.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not grant flashback if the target leaves the graveyard before resolution")
    void doesNotGrantFlashbackIfTargetLeavesGraveyard() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        harness.castSorcery(player1, 0, concentrate.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(concentrate.getId());
    }

    @Test
    @DisplayName("Granted flashback uses the sorcery's mana cost and exiles it after casting")
    void grantedFlashbackAllowsCastingSorcery() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, concentrate.getId());

        harness.castFlashback(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Concentrate");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Concentrate"));
    }

    @Test
    @DisplayName("Granted flashback still requires the sorcery's colored mana")
    void grantedFlashbackRequiresColoredMana() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, concentrate.getId());

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted flashback expires at the end of the turn")
    void grantedFlashbackExpiresAtEndOfTurn() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        harness.castAndResolveSorcery(player1, 0, concentrate.getId());
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(concentrate.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(concentrate.getId());
    }

    @Test
    @DisplayName("Recoup can be cast from the graveyard for its flashback cost")
    void recoupHasFlashback() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(new Recoup(), concentrate));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveFlashback(player1, 0, concentrate.getId());

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(concentrate.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Recoup"));
    }

    @Test
    @DisplayName("Only the targeted sorcery gains flashback")
    void onlyTargetedSorceryGainsFlashback() {
        Concentrate target = new Concentrate();
        Concentrate other = new Concentrate();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Expired flashback cannot be used on the next turn")
    void expiredFlashbackCannotBeUsed() {
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();
        harness.castAndResolveSorcery(player1, 0, concentrate.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Concentrate");
    }

    @Test
    @DisplayName("Granted flashback can use mana cost even when printed flashback is affordable")
    void grantedFlashbackCanUseCheaperCostWhenPrintedCostIsAffordable() {
        Recoup target = new Recoup();
        Concentrate concentrate = new Concentrate();
        harness.setGraveyard(player1, List.of(target, concentrate));
        harness.setHand(player1, List.of(new Recoup()));
        addRecoupMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFlashback(player1, 0, concentrate.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(concentrate.getId());
    }

    private void addRecoupMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
