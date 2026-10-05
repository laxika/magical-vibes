package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NivMizzetTheFiremind.class, GhostWarden.class, Gristleback.class})
class NivMizzetTheFiremindTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Draw a card draws and triggers 1 damage to a chosen player")
    void tapDrawsAndDealsDamageToPlayer() {
        addCreatureReady(player1, new NivMizzetTheFiremind());
        harness.setLibrary(player1, List.of(new GhostWarden()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Card was drawn
        harness.assertInHand(player1, "Ghost Warden");

        // Draw trigger awaits a target choice
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Draw trigger can deal its 1 damage to a creature, destroying a 1-toughness creature")
    void drawTriggerDamageDestroysCreature() {
        addCreatureReady(player1, new NivMizzetTheFiremind());
        harness.setLibrary(player1, List.of(new GhostWarden()));
        harness.addToBattlefield(player2, new Gristleback());
        harness.addToBattlefield(player2, new GhostWarden());

        UUID wizardId = harness.getPermanentId(player2, "Ghost Warden");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, wizardId);
        harness.passBothPriorities();

        // The 1/1 target dies; the 2/2 is unharmed
        harness.assertNotOnBattlefield(player2, "Ghost Warden");
        harness.assertOnBattlefield(player2, "Gristleback");
    }

    @Test
    @DisplayName("Draw trigger also fires when the controller draws from another effect")
    void independentControllerDrawTriggersDamage() {
        addCreatureReady(player1, new NivMizzetTheFiremind());
        harness.setLibrary(player1, List.of(new GhostWarden()));
        harness.setLife(player2, 20);

        draw(player1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Ghost Warden");

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent drawing a card does not trigger Niv-Mizzet")
    void opponentDrawDoesNotTriggerDamage() {
        addCreatureReady(player1, new NivMizzetTheFiremind());
        harness.setLibrary(player2, List.of(new GhostWarden()));
        harness.setLife(player2, 20);

        draw(player2);

        harness.assertInHand(player2, "Ghost Warden");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each controller draw triggers even while Niv-Mizzet is tapped")
    void repeatedDrawsWhileTappedTriggerSeparately() {
        var niv = addCreatureReady(player1, new NivMizzetTheFiremind());
        niv.setTapped(true);
        harness.setLibrary(player1, List.of(new GhostWarden(), new Gristleback()));
        harness.setLife(player2, 20);

        for (int i = 0; i < 2; i++) {
            draw(player1);
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Ghost Warden");
        harness.assertInHand(player1, "Gristleback");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Niv-Mizzet can target itself with its draw trigger")
    void drawTriggerCanTargetItself() {
        var niv = addCreatureReady(player1, new NivMizzetTheFiremind());
        harness.setLibrary(player1, List.of(new GhostWarden()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(niv.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, niv.getId());
        harness.passBothPriorities();

        assertThat(niv.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Niv-Mizzet, the Firemind");
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new NivMizzetTheFiremind());
        harness.setLibrary(player1, List.of(new GhostWarden()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.assertNotInHand(player1, "Ghost Warden");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Niv-Mizzet cannot activate its draw ability")
    void tappedSourceCannotActivate() {
        var niv = addCreatureReady(player1, new NivMizzetTheFiremind());
        niv.setTapped(true);
        harness.setLibrary(player1, List.of(new GhostWarden()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertNotInHand(player1, "Ghost Warden");
        assertThat(gd.stack).isEmpty();
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
