package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPlayFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.DisplayName;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.junit.jupiter.api.Test;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.ArrayList;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import java.util.List;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.amount.Fixed;

class ArkOfHungerTest extends BaseCardTest {

    // ===== Leave graveyard trigger =====

    @Test
    @DisplayName("Does not trigger when a card enters the graveyard from milling")
    void doesNotTriggerWhenCardEntersGraveyard() {
        addReadyArk(player1);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.addFirst(new Shock());

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Triggers when a card leaves the controller's graveyard")
    void triggersWhenCardLeavesGraveyard() {
        addReadyArk(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getCard().getName().equals("Ark of Hunger"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Triggers once when multiple cards are shuffled out of the controller's graveyard")
    void triggersOnceWhenGraveyardShuffledIntoLibrary() {
        addReadyArk(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        // Two cards leaving in a single event must produce exactly one trigger
        // (= the ability's two effects: deal damage + gain life), not one per card.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).allMatch(e -> e.getCard().getName().equals("Ark of Hunger"));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    // ===== Mill and play this turn =====

    @Test
    @DisplayName("Milling grants permission to play the milled card from graveyard this turn")
    void millingGrantsPlayPermission() {
        addReadyArk(player1);
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.graveyardPlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.graveyardPlayPermissionsExpireEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Can cast milled instant from graveyard using granted permission")
    void canCastMilledInstantFromGraveyard() {
        addReadyArk(player1);
        harness.setLife(player2, 20);
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.graveyardPlayPermissions).doesNotContainKey(shock.getId());
    }

    @Test
    @DisplayName("Granted graveyard play permission expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        addReadyArk(player1);
        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();

        assertThat(gd.graveyardPlayPermissions).containsKey(shock.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.graveyardPlayPermissions).doesNotContainKey(shock.getId());
        assertThat(gd.graveyardPlayPermissionsExpireEndOfTurn).isEmpty();
    }

    private void addReadyArk(Player player) {
        harness.addToBattlefield(player, new ArkOfHunger());
    }

    private int arkIndex(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ark of Hunger"))
                .findFirst()
                .map(gd.playerBattlefields.get(player.getId())::indexOf)
                .orElseThrow();
    }
}
