package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WavesOfAggression.class, GrizzlyBears.class, Plains.class})
class WavesOfAggressionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving untaps only creatures that attacked this turn and grants an extra combat/main pair")
    void resolvingUntapsAttackedCreaturesAndGrantsExtraCombat() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player1.getId());
        battlefield.forEach(p -> p.setSummoningSick(false));

        declareAttackers(player1, List.of(0));
        Permanent attackedBear = battlefield.get(0);
        Permanent nonAttackedBear = battlefield.get(1);
        nonAttackedBear.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new WavesOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(attackedBear.isTapped()).isFalse();
        assertThat(nonAttackedBear.isTapped()).isTrue();
        assertThat(harness.getGameData().additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Additional combat begins after postcombat main when Waves of Aggression resolves")
    void additionalCombatBeginsAfterPostcombatMain() {
        harness.setHand(player1, List.of(new WavesOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        // With no attacking creatures, skip directly to end of combat.
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Retrace casts Waves of Aggression from the graveyard by discarding a land and returns it to the graveyard")
    void retraceCastsFromGraveyardAndReturns() {
        harness.setGraveyard(player1, List.of(new WavesOfAggression()));
        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castRetrace(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isFalse();

        harness.passBothPriorities();

        // Retrace is not a flashback: the card returns to the graveyard, not exile.
        harness.assertInGraveyard(player1, "Waves of Aggression");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Waves of Aggression"));
        harness.assertInGraveyard(player1, "Plains");
    }

    @Test
    @DisplayName("Precombat casting inserts the extra combat/main pair before the normal combat")
    void precombatCastingPreservesNormalCombat() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WavesOfAggression(), "{3}{W}{W}");
        harness.passBothPriorities();

        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Retrace may be used again after resolution by paying and discarding another land")
    void retraceCanBeRepeated() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        WavesOfAggression waves = new WavesOfAggression();
        harness.setGraveyard(player1, List.of(waves));
        harness.setHand(player1, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();
        harness.castRetrace(player1, gd.playerGraveyards.get(player1.getId()).indexOf(waves), 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(Plains.class::isInstance).hasSize(2);
        harness.assertInGraveyard(player1, "Waves of Aggression");
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Retrace cannot discard a nonland card")
    void retraceRejectsNonlandDiscard() {
        harness.setGraveyard(player1, List.of(new WavesOfAggression()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Waves of Aggression");
    }

    @Test
    @CardUsed({Quicken.class})
    @DisplayName("Outside a main phase only the attacked creatures are untapped")
    void outsideMainPhaseDoesNotAddCombat() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        bear.setSummoningSick(false);
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new Plains()));
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.castFromHand(player1, new Quicken(), "{U}");
            harness.passBothPriorities();
            harness.castFromHand(player1, new WavesOfAggression(), "{3}{R}{R}");
            harness.passBothPriorities();
        });

        assertThat(bear.isTapped()).isFalse();
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }
}
