package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CravenGiant;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FanningTheFlames.class, Mountain.class, CravenGiant.class})
class FanningTheFlamesTest extends BaseCardTest {

    @Test
    @DisplayName("Fanning the Flames deals X damage to any target")
    void dealsXDamageToTargetPlayer() {
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Fanning the Flames can deal X damage to a creature")
    void dealsXDamageToTargetCreature() {
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        var target = harness.addToBattlefieldAndReturn(player2, new CravenGiant());

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    @DisplayName("Without buyback, Fanning the Flames goes to the graveyard")
    void withoutBuybackGoesToGraveyard() {
        var spell = new FanningTheFlames();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Paying buyback returns Fanning the Flames to hand after it resolves")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorceryWithBuyback(player1, 0, player2.getId());
        assertThat(gd.stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fanning the Flames cannot target a land")
    void cannotTargetLand() {
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);

        var mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Buyback pays for X damage and returns the same spell to hand")
    void nonzeroXWithBuyback() {
        var spell = new FanningTheFlames();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 3, player2.getId(), null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null, List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Buyback requires three mana in addition to the spell's normal cost")
    void cannotPayBuybackWithInsufficientMana() {
        harness.setHand(player1, List.of(new FanningTheFlames()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithBuyback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Buyback does not return the spell when its only target becomes illegal")
    void illegalTargetPreventsBuybackReturn() {
        var spell = new FanningTheFlames();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        var target = harness.addToBattlefieldAndReturn(player2, new CravenGiant());

        harness.castSorceryWithBuyback(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }
}
