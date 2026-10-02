package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DapperShieldmate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparasBodyguard.class, DapperShieldmate.class, Forest.class, GrizzlyBears.class})
class SparasBodyguardTest extends BaseCardTest {

    @Test
    void decliningHandChoicePutsShieldCounterOnSparasBodyguard() {
        harness.setHand(player1, List.of(new SparasBodyguard(), new GrizzlyBears()));
        addBodyguardMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualStaticEffectCardChoice.class);
        harness.handleCardChosen(player1, -1);

        Permanent bodyguard = findPermanent(player1, "Spara's Bodyguard");
        assertThat(bodyguard.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void chosenCreaturePerpetuallyEntersWithAnAdditionalShieldCounter() {
        harness.setHand(player1, List.of(new SparasBodyguard(), new GrizzlyBears()));
        addBodyguardMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");
        assertThat(grizzlyBears.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void noCreatureCardUsesTheShieldCounterFallbackWithoutPrompting() {
        harness.setHand(player1, List.of(new SparasBodyguard(), new Forest()));
        addBodyguardMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Spara's Bodyguard")
                .getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void getsPlusOnePlusOneForEachOtherShieldCounterAtBeginningOfCombat() {
        Permanent bodyguard = addCreatureReady(player1, new SparasBodyguard());
        Permanent shieldedCreature = harness.addToBattlefieldAndReturn(player1, new DapperShieldmate());
        shieldedCreature.setCounterCount(CounterType.SHIELD, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bodyguard)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bodyguard)).isEqualTo(5);
    }

    private void addBodyguardMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
