package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraveyardShovel.class, WalkingCorpse.class, TravelersAmulet.class})
class GraveyardShovelTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card from target player's graveyard gains controller 2 life")
    void exileCreatureGainsLife() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new TravelersAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Target player chooses which card to exile — choose the creature (index 0)
        harness.handleGraveyardCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getName()).isEqualTo("Traveler's Amulet");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Exiling a non-creature card does not gain life")
    void exileNonCreatureNoLifeGain() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new TravelersAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Choose the non-creature (Traveler's Amulet at index 1)
        harness.handleGraveyardCardChosen(player2, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()).getFirst().getName()).isEqualTo("Walking Corpse");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Traveler's Amulet"));
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Auto-exiles when graveyard has only one card — creature gains life")
    void autoExileSingleCreatureGainsLife() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Only one card — auto-exiled, no choice needed
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Auto-exiles when graveyard has only one card — non-creature no life")
    void autoExileSingleNonCreatureNoLife() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new TravelersAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Traveler's Amulet"));
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does nothing when target player's graveyard is empty")
    void emptyGraveyardDoesNothing() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target own graveyard")
    void canTargetOwnGraveyard() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        // Auto-exile the single creature
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Requires tap — cannot activate twice in same turn")
    void requiresTap() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new TravelersAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player2, 0);

        // Shovel is now tapped — second activation should fail
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                harness.activateAbility(player1, 0, null, player2.getId()));
    }

    @Test
    @DisplayName("Activation requires two mana")
    void cannotActivateWithOnlyOneMana() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Only the targeted player chooses, and the exile cannot be declined")
    void targetPlayerMustChoose() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new TravelersAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player2, 0);

        harness.assertNotInGraveyard(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Traveler's Amulet");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Uses the target player's graveyard at resolution")
    void cardArrivingBeforeResolutionCanBeExiled() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.setGraveyard(player2, List.of(new WalkingCorpse()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An ability still resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        harness.addToBattlefield(player1, new GraveyardShovel());
        harness.setGraveyard(player2, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Walking Corpse"));
        harness.assertLife(player1, 22);
    }
}
