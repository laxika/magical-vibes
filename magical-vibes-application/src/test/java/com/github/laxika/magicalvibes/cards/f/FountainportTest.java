package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fountainport.class})
class FountainportTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        harness.addToBattlefield(player1, new Fountainport());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void createsFishAndPaysLife() {
        harness.addToBattlefield(player1, new Fountainport());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent fish = findPermanent(player1, "Fish");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(fish.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(fish.getEffectivePower()).isEqualTo(1);
        assertThat(fish.getEffectiveToughness()).isEqualTo(1);
        assertThat(fish.isTapped()).isFalse();
    }

    @Test
    void createsTreasure() {
        harness.addToBattlefield(player1, new Fountainport());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(gqs.isArtifact(gd, treasure)).isTrue();
    }

    @Test
    void sacrificesTokenToDraw() {
        harness.addToBattlefield(player1, new Fountainport());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, new ArrayList<>());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fish");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void cannotSacrificeWithoutToken() {
        harness.addToBattlefield(player1, new Fountainport());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
