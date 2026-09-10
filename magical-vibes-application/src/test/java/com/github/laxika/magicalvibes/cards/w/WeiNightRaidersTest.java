package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FireAmbush;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeiNightRaiders.class, WeiInfantry.class, WeiEliteCompanions.class, FireAmbush.class})
class WeiNightRaidersTest extends BaseCardTest {

    @Test
    @DisplayName("Dealing combat damage to an opponent forces them to discard a chosen card")
    void combatDamageTriggersDiscard() {
        harness.setHand(player2, List.of(new WeiInfantry()));

        Permanent raiders = addCreatureReady(player1, new WeiNightRaiders());
        raiders.setAttacking(true);

        resolveCombat();

        // The damaged opponent chooses which card to discard.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Wei Infantry");
    }

    @Test
    @DisplayName("No discard when the opponent has an empty hand")
    void noDiscardWhenEmptyHand() {
        harness.setHand(player2, List.of());

        Permanent raiders = addCreatureReady(player1, new WeiNightRaiders());
        raiders.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("No trigger when the Raiders are blocked and deal no damage to the player")
    void noTriggerWhenBlocked() {
        harness.setHand(player2, List.of(new WeiInfantry()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        Permanent raiders = addCreatureReady(player1, new WeiNightRaiders());
        raiders.setAttacking(true);
        addCreatureReady(player2, new WeiEliteCompanions());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Opponent takes 2 combat damage from the unblocked Raiders")
    void opponentTakesCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());

        Permanent raiders = addCreatureReady(player1, new WeiNightRaiders());
        raiders.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent also triggers discard")
    void noncombatDamageToOpponentTriggersDiscard() {
        harness.setHand(player1, List.of(new FireAmbush()));
        harness.setHand(player2, List.of(new WeiInfantry()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addCreatureReady(player1, new WeiNightRaiders());

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Wei Infantry");
    }

    @Test
    @DisplayName("Damage to the Raiders' controller does not trigger discard")
    void damageToControllerDoesNotTriggerDiscard() {
        WeiInfantry cardInHand = new WeiInfantry();
        harness.setHand(player1, List.of(new FireAmbush(), cardInHand));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        addCreatureReady(player1, new WeiNightRaiders());

        harness.castSorcery(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardInHand);
    }
}
