package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Annul;
import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.s.StrokeOfGenius;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeiledSentry.class, CoralMerfolk.class, ClawsOfGix.class, StrokeOfGenius.class,
        Annul.class, VoltaicKey.class, DarkRitual.class})
class VeiledSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes an Illusion creature sized to an opponent's spell")
    void becomesCreatureWithOpponentsSpellManaValue() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new VeiledSentry());
        prepareOpponentCast();
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, sentry)).isTrue();
        assertThat(gqs.isEnchantment(gd, sentry)).isFalse();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, sentry)).containsExactly(CardSubtype.ILLUSION);
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a spell")
    void doesNotTriggerForControllerSpell() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new VeiledSentry());
        harness.setHand(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, sentry)).isTrue();
        assertThat(gqs.isCreature(gd, sentry)).isFalse();
    }

    @Test
    @DisplayName("Uses the chosen X value from an opponent's spell")
    void usesChosenXValueFromOpponentSpell() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new VeiledSentry());
        prepareOpponentCast();
        harness.setHand(player2, List.of(new StrokeOfGenius()));
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castInstant(player2, 0, 3, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(6);
    }

    @Test
    @DisplayName("Only the last of multiple queued triggers changes the sentry")
    void onlyLastOfMultipleQueuedTriggersChangesSentry() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new VeiledSentry());
        prepareOpponentCast();
        harness.setHand(player2, List.of(new StrokeOfGenius(), new DarkRitual()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0, 0, player1.getId());
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still becomes a creature if the triggering spell is countered")
    void becomesCreatureIfTriggeringSpellIsCountered() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new VeiledSentry());
        prepareOpponentCast();
        harness.setHand(player2, List.of(new VoltaicKey()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castArtifact(player2, 0);

        harness.setHand(player1, List.of(new Annul()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getFirst().getCard().getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, sentry)).isTrue();
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sentry)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent sentry = harness.addToBattlefieldAndReturn(player1, new VeiledSentry());
        animateWithOpponentSpell(sentry);

        prepareOpponentCast();
        harness.setHand(player2, List.of(new ClawsOfGix()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, sentry)).isEqualTo(2);
    }

    private void animateWithOpponentSpell(Permanent sentry) {
        prepareOpponentCast();
        harness.setHand(player2, List.of(new CoralMerfolk()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, sentry)).isTrue();
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
