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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarfieldVocalist.class, ElvishVisionary.class, Forest.class, ZoZuThePunisher.class})
class StarfieldVocalistTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles a triggered ability caused by a permanent entering under your control")
    void doublesControlledPermanentEnteringTrigger() {
        harness.addToBattlefield(player1, new StarfieldVocalist());

        harness.castFromHand(player1, new ElvishVisionary(), "{1}{G}");
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
        harness.playLand(player2, 0);

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

    @Test
    void doesNotDoubleOpponentsTriggeredAbilities() {
        harness.addToBattlefield(player1, new StarfieldVocalist());
        harness.addToBattlefield(player2, new ZoZuThePunisher());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    void multipleVocalistsEachAddOneTrigger() {
        harness.addToBattlefield(player1, new StarfieldVocalist());
        harness.addToBattlefield(player1, new StarfieldVocalist());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new ElvishVisionary(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void warpedVocalistStillAddsAnEtbTrigger() {
        harness.setHand(player1, List.of(new StarfieldVocalist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castFromHand(player1, new ElvishVisionary(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void warpExileWaitsForDelayedTriggerResolution() {
        StarfieldVocalist vocalist = new StarfieldVocalist();
        harness.setHand(player1, List.of(vocalist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Starfield Vocalist");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Starfield Vocalist");
        assertThat(gd.findExiledCard(vocalist.getId())).isNotNull();
    }

    @Test
    void normalCastDoesNotExileAtEndStep() {
        harness.castFromHand(player1, new StarfieldVocalist(), "{3}{U}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Starfield Vocalist");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void warpedCardCanBeRecastOnlyOnLaterTurnForNormalCost() {
        StarfieldVocalist vocalist = new StarfieldVocalist();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(vocalist));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(vocalist.getId())).isNotNull();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castFromExile(player1, vocalist.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, vocalist.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, vocalist.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Starfield Vocalist");
        assertThat(gd.findExiledCard(vocalist.getId())).isNull();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Starfield Vocalist");
        assertThat(gd.stack).isEmpty();
    }
}
