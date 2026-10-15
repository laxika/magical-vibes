package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldnightCommander.class, GrizzlyBears.class, FugitiveWizard.class, ThatcherRevolt.class})
class GoldnightCommanderTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering pumps all creatures you control until end of turn")
    void pumpsOwnCreaturesOnAllyEnter() {
        harness.addToBattlefield(player1, new GoldnightCommander());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        Permanent commander = findPermanent(player1, "Goldnight Commander");
        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(3);

        // The entering creature is on the battlefield when the trigger resolves, so it is pumped too.
        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, wizard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wizard)).isEqualTo(2);
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOff() {
        harness.addToBattlefield(player1, new GoldnightCommander());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger it")
    void noTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new GoldnightCommander());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering alone does not trigger the Commander's own ability")
    void doesNotTriggerForItsOwnEntry() {
        harness.setHand(player1, List.of(new GoldnightCommander()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent commander = findPermanent(player1, "Goldnight Commander");
        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolved boosts exclude later entrants, while multiple Commanders trigger independently")
    void laterEntrantsDoNotReceiveEarlierBoosts() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldnightCommander());
        harness.setHand(player1, List.of(new GoldnightCommander(), new GoldnightCommander()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent second = findPermanents(player1, "Goldnight Commander").get(1);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent third = findPermanents(player1, "Goldnight Commander").get(2);
        assertThat(gd.stack).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(2);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, third)).isEqualTo(4);
    }

    @Test
    @DisplayName("Each token entering simultaneously triggers a boost, without boosting opposing creatures")
    void simultaneousTokensEachTrigger() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new GoldnightCommander());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GoldnightCommander());
        harness.setHand(player1, List.of(new ThatcherRevolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(5);
        assertThat(findPermanents(player1, "Human")).hasSize(3).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        });
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }
}
