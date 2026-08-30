package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudOfDarkness.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class CloudOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -X/-X for permanent cards in your graveyard")
    void etbCountsOnlyYourPermanentCardsInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));

        castCloud(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new Forest()));

        castCloud(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature you control")
    void cannotTargetOwnCreature() {
        UUID ownCreatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new CloudOfDarkness()));
        addCloudMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(ownCreatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCloud(UUID targetId) {
        harness.setHand(player1, List.of(new CloudOfDarkness()));
        addCloudMana();
        harness.castCreature(player1, 0, List.of(targetId));
    }

    private void addCloudMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
