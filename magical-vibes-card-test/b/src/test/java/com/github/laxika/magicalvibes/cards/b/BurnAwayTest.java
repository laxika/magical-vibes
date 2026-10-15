package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WoollyLoxodon;
import com.github.laxika.magicalvibes.cards.w.WetlandSambar;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.ForceAway;
import com.github.laxika.magicalvibes.cards.m.MurderousCut;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BurnAway.class, WoollyLoxodon.class, WetlandSambar.class, Cancel.class, ForceAway.class,
        MurderousCut.class})
class BurnAwayTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage and exiles the dying creature controller's graveyard")
    void killsCreatureAndExilesItsControllersGraveyard() {
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.setGraveyard(player2, List.of(new ForceAway()));
        harness.setHand(player1, List.of(new BurnAway()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Wetland Sambar");
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Wetland Sambar", "Force Away");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Cancel", "Burn Away");
    }

    @Test
    @DisplayName("Does not exile a graveyard when the damaged creature survives")
    void doesNotExileGraveyardWhenCreatureSurvives() {
        harness.addToBattlefield(player2, new WoollyLoxodon());
        harness.setGraveyard(player2, List.of(new ForceAway()));
        harness.setHand(player1, List.of(new BurnAway()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Woolly Loxodon");
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Woolly Loxodon");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Force Away");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BurnAway()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesGraveyardWhenSurvivorDiesLaterThatTurn() {
        harness.addToBattlefield(player2, new WoollyLoxodon());
        harness.setGraveyard(player2, List.of(new Cancel()));
        harness.setHand(player1, List.of(new BurnAway(), new MurderousCut()));
        harness.addMana(player1, ManaColor.RED, 5);
        UUID targetId = harness.getPermanentId(player2, "Woolly Loxodon");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertOnBattlefield(player2, "Woolly Loxodon");
        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(6);
        harness.assertInGraveyard(player2, "Cancel");

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Woolly Loxodon", "Cancel");
    }

    @Test
    void delayedTriggerExpiresBeforeNextTurn() {
        harness.addToBattlefield(player2, new WoollyLoxodon());
        harness.setGraveyard(player2, List.of(new Cancel()));
        harness.setHand(player1, List.of(new BurnAway(), new MurderousCut()));
        harness.addMana(player1, ManaColor.RED, 5);
        UUID targetId = harness.getPermanentId(player2, "Woolly Loxodon");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0, targetId);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Cancel", "Woolly Loxodon");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void delayedTriggerDoesNotFollowCreatureReturnedToHandAndRecast() {
        harness.addToBattlefield(player1, new WoollyLoxodon());
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.setHand(player1, List.of(new BurnAway()));
        harness.setHand(player2, List.of(new ForceAway(), new MurderousCut()));
        harness.addMana(player1, ManaColor.RED, 5);
        UUID targetId = harness.getPermanentId(player1, "Woolly Loxodon");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertInHand(player1, "Woolly Loxodon");
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Woolly Loxodon"));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Cancel", "Burn Away", "Woolly Loxodon");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void ownCreatureDeathExilesBurnAwayAfterSpellFinishesResolving() {
        harness.addToBattlefield(player1, new WetlandSambar());
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.setHand(player1, List.of(new BurnAway()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Wetland Sambar"));

        harness.assertInGraveyard(player1, "Burn Away");
        harness.assertInGraveyard(player1, "Wetland Sambar");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Cancel", "Burn Away", "Wetland Sambar");
    }

    @Test
    void invalidTargetDoesNotCreateDelayedTrigger() {
        harness.addToBattlefield(player2, new WetlandSambar());
        harness.setGraveyard(player2, List.of(new Cancel()));
        harness.setHand(player1, List.of(new BurnAway()));
        harness.setHand(player2, List.of(new ForceAway()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);
        UUID targetId = harness.getPermanentId(player2, "Wetland Sambar");
        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player2, 0, targetId);
        resolveAllTriggers();

        harness.assertInHand(player2, "Wetland Sambar");
        harness.assertInGraveyard(player1, "Burn Away");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Cancel", "Force Away");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
