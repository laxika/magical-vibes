package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HundredTalonStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+0 and gains first strike")
    void pumpsAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The pump and first strike wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = player1.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell by tapping a white creature, staying in hand")
    void splicesOntoArcaneSpell() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.addToBattlefield(player1, new EliteVanguard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(arcaneShock, new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithSplice(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"), List.of(1),
                List.of(harness.getPermanentId(player1, "Elite Vanguard")));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Elite Vanguard").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Hundred-Talon Strike");
    }

    @Test
    @DisplayName("Cannot pay the splice cost by tapping a creature that is not white")
    void cannotTapNonWhiteCreatureForSplice() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(arcaneShock, new HundredTalonStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ownBearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID enemyBearId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, enemyBearId, List.of(1), List.of(ownBearId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
