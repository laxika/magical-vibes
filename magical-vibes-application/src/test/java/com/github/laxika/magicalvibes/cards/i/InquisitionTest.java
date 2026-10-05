package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AngryMob;
import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Inquisition.class, AngryMob.class, BogRats.class, TormodsCrypt.class})
class InquisitionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of white cards in the target player's hand")
    void dealsDamageForWhiteCardsInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Inquisition()));
        harness.setHand(player2, List.of(new AngryMob(), new BogRats(), new AngryMob()));
        addInquisitionMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 18);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand"));
    }

    @Test
    @DisplayName("Does not count nonwhite cards in the target player's hand")
    void ignoresNonwhiteCardsInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Inquisition()));
        harness.setHand(player2, List.of(new BogRats(), new BogRats()));
        addInquisitionMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Reveals an empty hand without dealing damage")
    void revealsEmptyHandWithoutDealingDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Inquisition()));
        harness.setHand(player2, List.of());
        addInquisitionMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target its controller")
    void canTargetItsController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Inquisition(), new AngryMob()));
        addInquisitionMana();

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void rejectsPermanentTarget() {
        harness.setHand(player1, List.of(new Inquisition()));
        addInquisitionMana();
        harness.addToBattlefield(player2, new BogRats());

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                gd.playerBattlefields.get(player2.getId()).getFirst().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts the target's white cards at resolution rather than when cast")
    void countsWhiteCardsAtResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Inquisition()));
        harness.setHand(player2, List.of(new AngryMob(), new AngryMob()));
        addInquisitionMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new AngryMob(), new BogRats()));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Angry Mob");
        harness.assertInHand(player2, "Bog Rats");
        harness.assertInGraveyard(player1, "Inquisition");
    }

    @Test
    @DisplayName("Colorless cards are revealed but do not contribute to damage")
    void ignoresColorlessCardsInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Inquisition()));
        harness.setHand(player2, List.of(new AngryMob(), new TormodsCrypt()));
        addInquisitionMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Angry Mob");
        harness.assertInHand(player2, "Tormod's Crypt");
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand")
                && log.plainText().contains("Angry Mob")
                && log.plainText().contains("Tormod's Crypt"));
    }

    private void addInquisitionMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
