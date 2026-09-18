package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzulaRuthlessFirebender.class, GrizzlyBears.class, MindRot.class})
class AzulaRuthlessFirebenderTest extends BaseCardTest {

    @Test
    void firebendingAddsRedManaUntilEndOfCombat() {
        addReadyAzula();

        declareAttackers(List.of(0));
        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void attackDiscardAwardsExperienceForEachPlayerWhoDiscardedThisTurn() {
        discardTwoCardsFromPlayer2();
        Permanent azula = addReadyAzula();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, azula)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, azula)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, azula, Keyword.MENACE)).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningDiscardStillAwardsExistingDiscardersExperience() {
        discardTwoCardsFromPlayer2();
        Permanent azula = addReadyAzula();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, azula)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, azula)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, azula, Keyword.MENACE)).isTrue();
    }

    @Test
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent azula = addReadyAzula();
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, azula)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, azula, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, azula)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, azula)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, azula, Keyword.MENACE)).isFalse();
    }

    private Permanent addReadyAzula() {
        return addCreatureReady(player1, new AzulaRuthlessFirebender());
    }

    private void discardTwoCardsFromPlayer2() {
        harness.setHand(player1, List.of(new MindRot()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
    }
}
