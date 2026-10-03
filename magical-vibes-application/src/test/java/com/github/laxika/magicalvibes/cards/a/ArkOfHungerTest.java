package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArkOfHunger.class, Disentomb.class, GrizzlyBears.class, Reminisce.class, Shock.class, Plains.class})
class ArkOfHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Does not trigger when a card enters the graveyard from milling")
    void doesNotTriggerWhenCardEntersGraveyard() {
        addReadyArk(player1);
        harness.setLibrary(player1, List.of(new Shock()));

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

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).allMatch(e -> e.getCard().getName().equals("Ark of Hunger"));

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

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).allMatch(e -> e.getCard().getName().equals("Ark of Hunger"));

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Milling grants permission to play the milled card from graveyard this turn")
    void millingGrantsPlayPermission() {
        addReadyArk(player1);
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.graveyardPlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.graveyardPlayPermissionsExpireEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("Can cast milled instant from graveyard using granted permission")
    void canCastMilledInstantFromGraveyard() {
        addReadyArk(player1);
        harness.setLife(player2, 20);
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.graveyardPlayPermissions).doesNotContainKey(shock.getId());
    }

    @Test
    @DisplayName("Granted graveyard play permission expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        addReadyArk(player1);
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));

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

    @Test
    @DisplayName("Can play a milled land and the departure triggers damage and life gain")
    void canPlayMilledLand() {
        addReadyArk(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Plains()));

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();
        harness.playLandFromGraveyard(player1, 0);

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotInGraveyard(player1, "Plains");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An empty library mills nothing and grants no play permission")
    void emptyLibraryDoesNothing() {
        addReadyArk(player1);
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Shock()));

        harness.activateAbility(player1, arkIndex(player1), null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.graveyardPlayPermissions).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cards leaving an opponent's graveyard do not trigger Ark")
    void opponentGraveyardDepartureDoesNotTrigger() {
        addReadyArk(player1);
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void addReadyArk(Player player) {
        harness.addToBattlefield(player, new ArkOfHunger());
    }

    private int arkIndex(Player player) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, "Ark of Hunger"));
    }
}
