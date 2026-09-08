package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PulseOfTheForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage and returns to hand when the opponent still has more life")
    void returnsToHandWhenOpponentStillHasMoreLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(handNames(player1)).containsExactly("Pulse of the Forge");
        assertThat(graveyardNames(player1)).doesNotContain("Pulse of the Forge");
    }

    @Test
    @DisplayName("Goes to the graveyard when the opponent does not have more life after the damage")
    void goesToGraveyardWhenOpponentDoesNotHaveMoreLifeAfterDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 13);
        castAt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
        assertThat(handNames(player1)).doesNotContain("Pulse of the Forge");
        assertThat(graveyardNames(player1)).containsExactly("Pulse of the Forge");
    }

    @Test
    @DisplayName("Deals damage to a planeswalker and checks its controller's life")
    void dealsDamageToPlaneswalkerAndChecksControllerLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        ElspethKnightErrant elspethCard = new ElspethKnightErrant();
        Permanent elspeth = new Permanent(elspethCard);
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(elspeth);

        castAt(elspeth.getId());

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(handNames(player1)).containsExactly("Pulse of the Forge");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PulseOfTheForge()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new PulseOfTheForge()));
        addMana();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<String> handNames(Player player) {
        return gd.playerHands.get(player.getId()).stream().map(card -> card.getName()).toList();
    }

    private List<String> graveyardNames(Player player) {
        return gd.playerGraveyards.get(player.getId()).stream().map(card -> card.getName()).toList();
    }
}
