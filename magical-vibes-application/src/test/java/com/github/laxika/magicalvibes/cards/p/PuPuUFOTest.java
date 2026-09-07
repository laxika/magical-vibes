package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PuPuUFO.class, Forest.class})
class PuPuUFOTest extends BaseCardTest {

    @Test
    @DisplayName("PuPu UFO puts a land from hand onto the battlefield")
    void putsLandFromHandOntoBattlefield() {
        addReadyUfo();
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("PuPu UFO's second ability uses the number of Towns at resolution")
    void setsBasePowerToTownCountAtResolution() {
        Permanent ufo = addReadyUfo();
        addTowns(player1, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        addTowns(player1, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ufo)).isEqualTo(3);
    }

    @Test
    @DisplayName("PuPu UFO's base-power change wears off at end of turn")
    void basePowerChangeWearsOffAtEndOfTurn() {
        Permanent ufo = addReadyUfo();
        addTowns(player1, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ufo)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ufo)).isNotEqualTo(3);
    }

    private Permanent addReadyUfo() {
        Permanent ufo = harness.addToBattlefieldAndReturn(player1, new PuPuUFO());
        ufo.setSummoningSick(false);
        return ufo;
    }

    private void addTowns(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            Card town = TestCards.mutableCard(new Permanent(new Forest()));
            town.setSubtypes(List.of(CardSubtype.TOWN));
            harness.addToBattlefield(player, town);
        }
    }
}
