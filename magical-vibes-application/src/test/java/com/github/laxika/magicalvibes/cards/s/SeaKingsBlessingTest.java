package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({SeaKingsBlessing.class, KoboldsOfKherKeep.class, Karakas.class})
class SeaKingsBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Makes one or more target creatures blue until end of turn")
    void makesAllTargetsBlue() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());

        cast(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, opposingCreature)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("The color change wears off at end of turn")
    void colorChangeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());

        cast(List.of(creature.getId()));

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.BLUE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, creature)).containsExactly(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent karakas = harness.addToBattlefieldAndReturn(player2, new Karakas());
        harness.setHand(player1, List.of(new SeaKingsBlessing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(karakas.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SeaKingsBlessing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }
}
