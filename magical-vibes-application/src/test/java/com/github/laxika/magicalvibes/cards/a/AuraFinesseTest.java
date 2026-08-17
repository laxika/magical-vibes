package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuraFinesseTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches a controlled Aura to the target creature and draws a card")
    void attachesAuraAndDrawsCard() {
        Permanent originalHost = addCreature(player1);
        Permanent destination = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, originalHost);
        Card drawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Cannot target an Aura controlled by an opponent")
    void cannotTargetOpponentsAura() {
        Permanent host = addCreature(player2);
        Permanent opponentAura = addAuraAttachedTo(player2, host);
        Permanent destination = addCreature(player1);

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(opponentAura.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura you control");
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new Pacifism());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
