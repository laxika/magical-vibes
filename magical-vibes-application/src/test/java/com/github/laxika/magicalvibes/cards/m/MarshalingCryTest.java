package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarshalingCry.class, GrizzlyBears.class})
class MarshalingCryTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+1 and vigilance until end of turn")
    void boostsOwnCreaturesAndGrantsVigilanceUntilEndOfTurn() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherOwnBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherOwnBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherOwnBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, otherOwnBears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.VIGILANCE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Flashback applies the same effect and exiles Marshaling Cry")
    void flashbackAppliesEffectAndExilesSpell() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MarshalingCry()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Marshaling Cry");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Marshaling Cry"));
    }

    @Test
    @DisplayName("Cycling discards Marshaling Cry and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new MarshalingCry()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Marshaling Cry");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
