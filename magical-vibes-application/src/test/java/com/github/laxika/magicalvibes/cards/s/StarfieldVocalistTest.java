package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZoZuThePunisher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarfieldVocalist.class, ElvishVisionary.class, Forest.class, ZoZuThePunisher.class})
class StarfieldVocalistTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a triggered ability caused by a permanent entering under your control")
    void doublesControlledPermanentEnteringTrigger() {
        harness.addToBattlefield(player1, new StarfieldVocalist());

        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Doubles a triggered ability caused by an opponent's permanent entering")
    void doublesOpponentPermanentEnteringTrigger() {
        harness.addToBattlefield(player1, new StarfieldVocalist());
        harness.addToBattlefield(player1, new ZoZuThePunisher());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Forest()));
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Can be cast using its Warp cost")
    void canBeCastForWarpCost() {
        StarfieldVocalist vocalist = new StarfieldVocalist();
        harness.setHand(player1, List.of(vocalist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Starfield Vocalist");
    }
}
