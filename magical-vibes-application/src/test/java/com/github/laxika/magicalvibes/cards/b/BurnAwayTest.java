package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BurnAwayTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Deals 6 damage and exiles the dying creature controller's graveyard")
    void killsCreatureAndExilesItsControllersGraveyard() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new LightningBolt());
        gd.playerGraveyards.get(player2.getId()).add(new Shock());
        harness.setHand(player1, List.of(new BurnAway()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Shock");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Lightning Bolt", "Burn Away");
    }

    @Test
    @DisplayName("Does not exile a graveyard when the damaged creature survives")
    void doesNotExileGraveyardWhenCreatureSurvives() {
        harness.addToBattlefield(player2, new AvatarOfMight());
        gd.playerGraveyards.get(player2.getId()).add(new Shock());
        harness.setHand(player1, List.of(new BurnAway()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Shock");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BurnAway()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
