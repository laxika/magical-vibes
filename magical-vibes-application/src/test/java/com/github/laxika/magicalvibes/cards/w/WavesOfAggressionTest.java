package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);

        // CR 508.8: no attacking creatures, skip directly to end of combat
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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Waves of Aggression"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Waves of Aggression"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    private void declareAttackers(Player attacker, List<Integer> attackers) {
        GameData gd = harness.getGameData();
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.getGameService().declareAttackers(gd, attacker, attackers);
    }
}
