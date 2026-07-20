package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WarfireJavelineerTest extends BaseCardTest {

    private Permanent targetOf(UUID id) {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    private UUID castAtOpponentBear() {
        harness.setHand(player1, List.of(new WarfireJavelineer()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gs.playCard(gd, player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // Resolve creature → ETB triggers
        harness.passBothPriorities(); // Resolve ETB
        return targetId;
    }

    @Test
    @DisplayName("ETB deals damage equal to instants and sorceries in own graveyard")
    void etbDamageCountsInstantsAndSorceries() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);

        // 2 instants + 1 sorcery = 3 damage
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player1.getId()).add(new Divination());

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
        gd.playerGraveyards.get(player2.getId()).add(new Shock());
        gd.playerGraveyards.get(player2.getId()).add(new Divination());

        UUID targetId = castAtOpponentBear();

        assertThat(targetOf(targetId).getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-instant/sorcery cards in graveyard do not count")
    void etbIgnoresOtherCardTypes() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(8);
        harness.addToBattlefield(player2, bear);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

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
}
