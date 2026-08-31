package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DanceOfMany.class, FountainOfYouth.class, Squire.class})
class DanceOfManyTest extends BaseCardTest {

    private void castDanceCopying(UUID targetId) {
        harness.setHand(player1, List.of(new DanceOfMany()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // enchantment resolves, ETB trigger goes on stack
        harness.passBothPriorities(); // ETB trigger resolves -> token copy created
    }

    private Permanent tokenCopy(Player player) {
        return findPermanents(player, "Squire").stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElse(null);
    }

    private Permanent danceOfMany(Player player) {
        return findPermanents(player, "Dance of Many").stream()
                .findFirst().orElse(null);
    }

    @Test
    @DisplayName("ETB creates a token copy of the target nontoken creature")
    void etbCreatesTokenCopy() {
        harness.addToBattlefield(player2, new Squire());
        UUID squireId = harness.getPermanentId(player2, "Squire");

        castDanceCopying(squireId);

        Permanent token = tokenCopy(player1);
        assertThat(token).isNotNull();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(danceOfMany(player1)).isNotNull();
    }

    @Test
    @DisplayName("Cannot target a token creature")
    void cannotTargetTokenCreature() {
        harness.addToBattlefield(player2, new Squire());
        castDanceCopying(harness.getPermanentId(player2, "Squire"));
        Permanent token = tokenCopy(player1);
        assertThat(token).isNotNull();

        harness.setHand(player1, List.of(new DanceOfMany()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, token.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature");
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

    @Test
    @DisplayName("When the enchantment leaves the battlefield, the token is exiled")
    void enchantmentLeavingExilesToken() {
        harness.addToBattlefield(player2, new Squire());
        castDanceCopying(harness.getPermanentId(player2, "Squire"));
        assertThat(tokenCopy(player1)).isNotNull();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, danceOfMany(player1)));
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve LTB trigger -> exile the token

        assertThat(tokenCopy(player1)).isNull();
    }

    @Test
    @DisplayName("When the token leaves the battlefield, the enchantment is sacrificed")
    void tokenLeavingSacrificesEnchantment() {
        harness.addToBattlefield(player2, new Squire());
        castDanceCopying(harness.getPermanentId(player2, "Squire"));
        Permanent token = tokenCopy(player1);
        assertThat(token).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // resolve LTB trigger -> sacrifice the enchantment

        assertThat(danceOfMany(player1)).isNull();
        harness.assertInGraveyard(player1, "Dance of Many");
    }

    @Test
    @DisplayName("Declining to pay {U}{U} sacrifices the enchantment and exiles the token")
    void decliningUpkeepPaymentSacrificesEnchantmentAndExilesToken() {
        harness.addToBattlefield(player2, new Squire());
        castDanceCopying(harness.getPermanentId(player2, "Squire"));

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
        harness.addToBattlefield(player2, new Squire());
        castDanceCopying(harness.getPermanentId(player2, "Squire"));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(danceOfMany(player1)).isNotNull();
        assertThat(tokenCopy(player1)).isNotNull();
    }

    @Test
    @DisplayName("Leaving before the ETB ability resolves still creates an unlinked token")
    void leavingBeforeEtbResolutionStillCreatesUnlinkedToken() {
        harness.addToBattlefield(player2, new Squire());
        UUID squireId = harness.getPermanentId(player2, "Squire");
        harness.setHand(player1, List.of(new DanceOfMany()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, squireId);
        harness.passBothPriorities();

        Permanent dance = danceOfMany(player1);
        assertThat(dance).isNotNull();
        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dance));
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tokenCopy(player1)).isNotNull();
        assertThat(danceOfMany(player1)).isNull();
    }
}
