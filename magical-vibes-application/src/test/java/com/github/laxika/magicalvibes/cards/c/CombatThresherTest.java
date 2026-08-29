package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CombatThresherTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new CombatThresher()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void prototypeCastUsesAlternateCharacteristicsAndStillDraws() {
        harness.setHand(player1, List.of(new CombatThresher()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Forest()));

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent thresher = findPermanent(player1, "Combat Thresher");
        assertThat(gqs.getEffectivePower(gd, thresher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thresher)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, thresher)).containsExactly(CardColor.WHITE);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }
}
