package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SarcatogTest extends BaseCardTest {

    @Test
    void exilingTwoGraveyardCardsBoostsSarcatog() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(sarcatog.getPowerModifier()).isEqualTo(1);
        assertThat(sarcatog.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    void sacrificingAnArtifactBoostsSarcatog() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(sarcatog.getPowerModifier()).isEqualTo(1);
        assertThat(sarcatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    void cannotExileTwoCardsWithoutTwoCardsInGraveyard() {
        harness.addToBattlefield(player1, new Sarcatog());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsWearOffAtEndOfTurn() {
        Permanent sarcatog = harness.addToBattlefieldAndReturn(player1, new Sarcatog());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sarcatog.getPowerModifier()).isZero();
        assertThat(sarcatog.getToughnessModifier()).isZero();
    }
}
