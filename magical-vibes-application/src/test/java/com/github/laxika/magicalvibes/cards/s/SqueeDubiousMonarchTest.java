package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPicker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SqueeDubiousMonarch.class, GoblinPicker.class})
class SqueeDubiousMonarchTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking Goblin token")
    void attackingCreatesGoblinToken() {
        addCreatureReady(player1, new SqueeDubiousMonarch());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(1);
        Permanent goblin = findPermanents(player1, "Goblin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(goblin.isTapped()).isTrue();
        assertThat(goblin.isAttackedThisTurn()).isTrue();
        assertThat(goblin.getCard().getPower()).isEqualTo(1);
        assertThat(goblin.getCard().getToughness()).isEqualTo(1);
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Can be cast from the graveyard for {3}{R} by exiling four other cards")
    void castFromGraveyardExilesFourOtherCards() {
        SqueeDubiousMonarch squee = new SqueeDubiousMonarch();
        GoblinPicker first = new GoblinPicker();
        GoblinPicker second = new GoblinPicker();
        GoblinPicker third = new GoblinPicker();
        GoblinPicker fourth = new GoblinPicker();
        harness.setGraveyard(player1, List.of(squee, first, second, third, fourth));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Squee, Dubious Monarch");
    }

    @Test
    @DisplayName("Cannot be cast from the graveyard without four other cards")
    void requiresFourOtherCardsToCastFromGraveyard() {
        harness.setGraveyard(player1, List.of(
                new SqueeDubiousMonarch(), new GoblinPicker(), new GoblinPicker(), new GoblinPicker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotExileSqueeForItsOwnCastingCost() {
        harness.setGraveyard(player1, List.of(new SqueeDubiousMonarch(),
                new GoblinPicker(), new GoblinPicker(), new GoblinPicker(), new GoblinPicker()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Squee, Dubious Monarch");
    }

    @Test
    void cannotExileTheSameCardMoreThanOnce() {
        harness.setGraveyard(player1, List.of(new SqueeDubiousMonarch(),
                new GoblinPicker(), new GoblinPicker(), new GoblinPicker(), new GoblinPicker()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 3)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void normalManaCostIsInsufficientForGraveyardCasting() {
        harness.setGraveyard(player1, List.of(new SqueeDubiousMonarch(),
                new GoblinPicker(), new GoblinPicker(), new GoblinPicker(), new GoblinPicker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Squee, Dubious Monarch");
    }

    @Test
    void graveyardCastingPreservesUnchosenCardsWhenSqueeIsNotFirst() {
        GoblinPicker unchosen = new GoblinPicker();
        SqueeDubiousMonarch squee = new SqueeDubiousMonarch();
        harness.setGraveyard(player1, List.of(new GoblinPicker(), unchosen, squee,
                new GoblinPicker(), new GoblinPicker(), new GoblinPicker()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castFromGraveyard(player1, 2, List.of(5, 0, 4, 3));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(4).doesNotContain(squee, unchosen);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Squee, Dubious Monarch");
    }

    @Test
    void graveyardPermissionDoesNotAllowCastingDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new SqueeDubiousMonarch(),
                new GoblinPicker(), new GoblinPicker(), new GoblinPicker(), new GoblinPicker()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Squee, Dubious Monarch");
    }

    @Test
    void canAttackImmediatelyAfterBeingCastAndTokenDealsCombatDamage() {
        harness.castFromHand(player1, new SqueeDubiousMonarch(), "{2}{R}");
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, 17);
        assertThat(countPermanents(player1, "Goblin")).isEqualTo(1);
    }
}
