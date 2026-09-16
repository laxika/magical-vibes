package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessCharge.class, AvenFisher.class, Plains.class})
class RecklessChargeTest extends BaseCardTest {

    @Test
    void targetCreatureGetsPowerBoostAndHasteUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenFisher());
        harness.setHand(player1, List.of(new RecklessCharge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void boostAndHasteWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenFisher());
        harness.setHand(player1, List.of(new RecklessCharge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void flashbackResolvesAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenFisher());
        harness.setGraveyard(player1, List.of(new RecklessCharge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Reckless Charge");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Reckless Charge"));
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new RecklessCharge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenFisher());
        harness.setHand(player1, List.of(new RecklessCharge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }
}
