package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.g.GrowthSpiral;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.cards.s.SenateCourier;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevivalRevenge.class, GrizzlyBears.class, HillGiant.class,
        GrowthSpiral.class, PlatinumAngel.class, SenateCourier.class})
class RevivalRevengeTest extends BaseCardTest {

    private static final int REVIVAL = 0;
    private static final int REVENGE = 1;

    @Test
    @DisplayName("Revival returns a targeted creature card with mana value 3 or less")
    void revivalReturnsCheapCreature() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, REVIVAL, bears.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Revival does not target a creature card with mana value greater than 3")
    void revivalRejectsExpensiveCreature() {
        Card hillGiant = new HillGiant();
        harness.setGraveyard(player1, List.of(hillGiant));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REVIVAL, hillGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Revenge doubles your life and makes a target opponent lose half theirs")
    void revengeChangesBothLifeTotals() {
        harness.setLife(player1, 11);
        harness.setLife(player2, 19);
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, REVENGE, player2.getId());

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("Revenge cannot target its caster")
    void revengeRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID playerId = player1.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REVENGE, playerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revivalReturnsCreatureWithManaValueExactlyThreeUsingMixedHybridPayment() {
        Card courier = new SenateCourier();
        harness.setGraveyard(player1, List.of(courier));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, REVIVAL, courier.getId());

        harness.assertOnBattlefield(player1, "Senate Courier");
        harness.assertNotInGraveyard(player1, "Senate Courier");
    }

    @Test
    void revivalCannotTargetOpponentsGraveyard() {
        Card courier = new SenateCourier();
        harness.setGraveyard(player2, List.of(courier));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REVIVAL, courier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revivalCannotTargetCheapNoncreatureCard() {
        Card spiral = new GrowthSpiral();
        harness.setGraveyard(player1, List.of(spiral));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, REVIVAL, spiral.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void revivalDoesNotReturnAnotherCreatureWhenTargetLeavesGraveyard() {
        Card target = new SenateCourier();
        Card other = new SenateCourier();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, REVIVAL, target.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Senate Courier");
        harness.assertInGraveyard(player1, "Senate Courier");
    }

    @Test
    void revengeHalvesEvenOpponentLifeTotal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, REVENGE, player2.getId());

        harness.assertLife(player1, 40);
        harness.assertLife(player2, 10);
    }

    @Test
    void revengeUsesLifeTotalsAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, REVENGE, player2.getId());
        harness.setLife(player1, 7);
        harness.setLife(player2, 13);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 6);
    }

    @Test
    void revengeDoublesNegativeControllerLifeTotalByLosingLife() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        harness.setLife(player1, -5);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new RevivalRevenge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, REVENGE, player2.getId());

        harness.assertLife(player1, -10);
        harness.assertLife(player2, 10);
    }
}
