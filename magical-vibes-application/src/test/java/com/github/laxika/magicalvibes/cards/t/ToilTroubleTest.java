package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MazeBehemoth;
import com.github.laxika.magicalvibes.cards.m.MelekIzzetParagon;
import com.github.laxika.magicalvibes.cards.r.RiotControl;
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

/**
 * Toil // Trouble is one card whose two halves (and their fusion) are the three modes of a single
 * modal sorcery, each paying its own total cost.
 */
@CardUsed({ToilTrouble.class, MazeBehemoth.class, MelekIzzetParagon.class, RiotControl.class})
class ToilTroubleTest extends BaseCardTest {

    private static final int TOIL = 0;
    private static final int TROUBLE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Toil makes the targeted player draw two and lose 2 life")
    void toilDrawsAndLosesLife() {
        harness.setLibrary(player2, List.of(new MazeBehemoth(), new MazeBehemoth(), new MazeBehemoth()));
        harness.setHand(player2, List.of());

        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, TOIL, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Trouble deals damage equal to the target player's hand size")
    void troubleDamagesEqualToHandSize() {
        harness.setHand(player2, List.of(new MazeBehemoth(), new MazeBehemoth(), new MazeBehemoth()));

        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, TROUBLE, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Trouble is castable off red mana alone — Trouble costs {2}{R}")
    void troubleIsPaidWithItsOwnCost() {
        harness.setHand(player2, List.of(new MazeBehemoth()));

        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, TROUBLE, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Fuse on the same player: Toil's draws count toward Trouble's damage")
    void fuseSamePlayerCountsDrawnCards() {
        harness.setLibrary(player2, List.of(new MazeBehemoth(), new MazeBehemoth(), new MazeBehemoth()));
        harness.setHand(player2, List.of(new MazeBehemoth()));

        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, FUSE, List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        // Started with 1 card, drew 2 → hand size 3 for Trouble; also lost 2 from Toil.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Fuse can target two different players")
    void fuseUsesIndependentTargets() {
        harness.setLibrary(player1, List.of(new MazeBehemoth(), new MazeBehemoth()));
        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.setHand(player2, List.of(new MazeBehemoth(), new MazeBehemoth()));

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, FUSE, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        // Toil on self: cast removes the card (0), then draws 2; loses 2 life.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        // Trouble on opponent with 2 cards.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fuse cannot be cast for only one half's mana")
    void fuseRequiresBothHalvesCost() {
        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        UUID playerId = player2.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE, List.of(playerId, playerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MazeBehemoth());

        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID creatureId = creature.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, TOIL, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Trouble deals no damage to a player with an empty hand")
    void troubleWithEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, TROUBLE, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Toil // Trouble");
    }

    @Test
    @DisplayName("Trouble counts the hand at resolution rather than when cast")
    void troubleUsesCurrentHandSize() {
        harness.setHand(player2, List.of(new MazeBehemoth()));
        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, TROUBLE, player2.getId());

        harness.setHand(player2, List.of(new MazeBehemoth(), new MazeBehemoth(), new MazeBehemoth()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Fuse cannot be cast from the library with Melek")
    void fuseCannotBeCastFromLibrary() {
        harness.addToBattlefield(player1, new MelekIzzetParagon());
        harness.setLibrary(player1, List.of(new ToilTrouble()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCardFromLibraryTop(gd, player1, FUSE, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This mode can only be cast from hand");
    }

    @Test
    @DisplayName("Damage prevention stops Trouble but not Toil's life loss")
    void preventionDoesNotStopToilLifeLoss() {
        harness.setHand(player1, List.of(new ToilTrouble()));
        harness.setHand(player2, List.of(new RiotControl()));
        harness.setLibrary(player2, List.of(new MazeBehemoth(), new MazeBehemoth(), new MazeBehemoth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castModalSorcery(player1, 0, FUSE, List.of(player2.getId(), player2.getId()));
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
