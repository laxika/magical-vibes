package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarfireJavelineer.class, GrizzlyBears.class, Shock.class, Divination.class})
class WarfireJavelineerTest extends BaseCardTest {

    private Permanent targetOf(UUID id) {
        return findPermanents(player2, "Grizzly Bears").stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    private UUID castAtOpponentBear() {
        harness.setHand(player1, List.of(new WarfireJavelineer()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();
        return targetId;
    }

    @Test
    @DisplayName("ETB deals damage equal to instants and sorceries in own graveyard")
    void etbDamageCountsInstantsAndSorceries() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);

        // 2 instants + 1 sorcery = 3 damage
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Divination()));

        UUID targetId = castAtOpponentBear();

        assertThat(gd.stack).isEmpty();
        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Only the controller's graveyard counts, not the opponent's")
    void etbIgnoresOpponentGraveyard() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        // Opponent's instants/sorceries must not contribute
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));

        UUID targetId = castAtOpponentBear();

        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-instant/sorcery cards in graveyard do not count")
    void etbIgnoresOtherCardTypes() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));

        UUID targetId = castAtOpponentBear();

        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Empty graveyard deals no damage")
    void etbDealsNoDamageWithEmptyGraveyard() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);

        UUID targetId = castAtOpponentBear();

        assertThat(gd.stack).isEmpty();
        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a creature the controller controls")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new WarfireJavelineer()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("An instant cast in response contributes to damage at resolution")
    void countsInstantThatResolvesInResponse() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new WarfireJavelineer(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Removing cards from the graveyard before resolution reduces damage")
    void countsCurrentGraveyardAtResolution() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.setHand(player1, List.of(new WarfireJavelineer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());
        resolveAllTriggers();

        assertThat(targetOf(targetId).getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The damage trigger resolves after Javelineer is killed in response")
    void triggerResolvesAfterSourceDies() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.setHand(player1, List.of(new WarfireJavelineer()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Warfire Javelineer");
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.assertInGraveyard(player1, "Warfire Javelineer");
        resolveAllTriggers();

        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger has no effect if its target dies in response")
    void triggerDoesNotDamagePlayerWhenTargetDies() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.setHand(player1, List.of(new WarfireJavelineer(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
