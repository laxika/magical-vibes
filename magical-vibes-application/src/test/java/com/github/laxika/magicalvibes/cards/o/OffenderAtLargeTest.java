package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.Galvanize;
import com.github.laxika.magicalvibes.cards.s.SanitationAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OffenderAtLarge.class, SanitationAutomaton.class, Galvanize.class})
class OffenderAtLargeTest extends BaseCardTest {

    @Test
    void entersAndBoostsUpToOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        harness.setHand(player1, List.of(new OffenderAtLarge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void canEnterWithoutChoosingATarget() {
        harness.setHand(player1, List.of(new OffenderAtLarge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Offender at Large");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpBoostsUpToOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        harness.setHand(player1, List.of(new OffenderAtLarge()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent offender = findPermanent(player1, "Offender at Large");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(offender));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(offender.isFaceDown()).isFalse();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void faceDownEntryDoesNotTriggerTheBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        harness.setHand(player1, List.of(new OffenderAtLarge()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Offender at Large").isFaceDown()).isTrue();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpCanDeclineATargetEvenWithCreaturesAvailable() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SanitationAutomaton());
        harness.setHand(player1, List.of(new OffenderAtLarge()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent offender = findPermanent(player1, "Offender at Large");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(offender));
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(offender.isFaceDown()).isFalse();
        assertThat(offender.getPowerModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canBoostAnOpponentsCreatureAndBoostExpiresAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SanitationAutomaton());
        harness.setHand(player1, List.of(new OffenderAtLarge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void disguiseWardCountersAnOpponentsSpellWhenTheyCannotPay() {
        harness.setHand(player1, List.of(new OffenderAtLarge()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent offender = findPermanent(player1, "Offender at Large");
        harness.setHand(player2, List.of(new Galvanize()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, offender.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Galvanize");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(offender);
        assertThat(offender.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
