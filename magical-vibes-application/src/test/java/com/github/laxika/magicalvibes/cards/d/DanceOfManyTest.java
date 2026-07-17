package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DanceOfManyTest extends BaseCardTest {

    private void castDanceCopying(UUID targetId) {
        harness.setHand(player1, List.of(new DanceOfMany()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // enchantment resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves -> token copy created
    }

    private Permanent tokenCopy(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
    }

    private Permanent danceOfMany(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dance of Many"))
                .findFirst().orElse(null);
    }

    // ===== ETB token copy =====

    @Test
    @DisplayName("ETB creates a token copy of the target nontoken creature")
    void etbCreatesTokenCopy() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castDanceCopying(bearsId);

        Permanent token = tokenCopy(player1);
        assertThat(token).isNotNull();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(danceOfMany(player1)).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new DanceOfMany()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature");
    }

    // ===== Linked leaves-the-battlefield triggers =====

    @Test
    @DisplayName("When the enchantment leaves the battlefield, the token is exiled")
    void enchantmentLeavingExilesToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDanceCopying(harness.getPermanentId(player2, "Grizzly Bears"));
        assertThat(tokenCopy(player1)).isNotNull();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, danceOfMany(player1));
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve LTB trigger -> exile the token

        assertThat(tokenCopy(player1)).isNull();
    }

    @Test
    @DisplayName("When the token leaves the battlefield, the enchantment is sacrificed")
    void tokenLeavingSacrificesEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDanceCopying(harness.getPermanentId(player2, "Grizzly Bears"));
        Permanent token = tokenCopy(player1);
        assertThat(token).isNotNull();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve LTB trigger -> sacrifice the enchantment

        assertThat(danceOfMany(player1)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dance of Many"));
    }

    // ===== Upkeep sacrifice-unless-pay =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, trigger fires
    }

    @Test
    @DisplayName("Declining to pay {U}{U} sacrifices the enchantment and exiles the token")
    void decliningUpkeepPaymentSacrificesEnchantmentAndExilesToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDanceCopying(harness.getPermanentId(player2, "Grizzly Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false); // decline -> sacrifice enchantment
        harness.passBothPriorities(); // resolve LTB trigger -> exile the token

        assertThat(danceOfMany(player1)).isNull();
        assertThat(tokenCopy(player1)).isNull();
    }

    @Test
    @DisplayName("Paying {U}{U} keeps the enchantment and its token")
    void payingUpkeepKeepsEnchantmentAndToken() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castDanceCopying(harness.getPermanentId(player2, "Grizzly Bears"));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(danceOfMany(player1)).isNotNull();
        assertThat(tokenCopy(player1)).isNotNull();
    }
}
