package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.cards.v.VoraciousTyphon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightBreathCatoblepas.class, VoraciousTyphon.class, SternDismissal.class})
class BlightBreathCatoblepasTest extends BaseCardTest {

    @Test
    void etbGivesOpponentCreatureMinusPowerAndToughnessEqualToBlackDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());

        castCatoblepas(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void onlyControllersBlackDevotionIsCounted() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        harness.addToBattlefield(player2, new BlightBreathCatoblepas());

        castCatoblepas(target);

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void debuffExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());

        castCatoblepas(target);
        assertThat(target.getEffectivePower()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VoraciousTyphon());
        harness.setHand(player1, List.of(new BlightBreathCatoblepas()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void additionalBlackPermanentBeforeResolutionIncreasesDevotionAndKillsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());

        castCatoblepasUntilTriggerPending(target);
        harness.addToBattlefield(player1, new BlightBreathCatoblepas());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Voracious Typhon");
        harness.assertInGraveyard(player2, "Voracious Typhon");
    }

    @Test
    void removingSourceBeforeResolutionLeavesZeroDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        castCatoblepasUntilTriggerPending(target);
        Permanent source = findPermanent(player1, "Blight-Breath Catoblepas");

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInHand(player1, "Blight-Breath Catoblepas");
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    void removedSourceTriggerStillUsesRemainingBlackDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BlightBreathCatoblepas());
        castCatoblepasUntilTriggerPending(target);
        Permanent source = findPermanents(player1, "Blight-Breath Catoblepas").stream()
                .filter(permanent -> !permanent.getId().equals(other.getId()))
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void devotionChangesAfterResolutionDoNotChangeDebuff() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        castCatoblepas(target);
        Permanent source = findPermanent(player1, "Blight-Breath Catoblepas");
        harness.addToBattlefield(player1, new BlightBreathCatoblepas());

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, source.getId());

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void blackCardsInHandAndGraveyardDoNotCountTowardDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        harness.setGraveyard(player1, List.of(new BlightBreathCatoblepas()));
        castCatoblepasUntilTriggerPending(target);
        harness.setHand(player1, List.of(new BlightBreathCatoblepas()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void canEnterWhenOpponentHasNoCreature() {
        harness.addToBattlefield(player1, new VoraciousTyphon());
        harness.castFromHand(player1, new BlightBreathCatoblepas(), "{4}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blight-Breath Catoblepas");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castCatoblepas(Permanent target) {
        castCatoblepasUntilTriggerPending(target);
        harness.passBothPriorities();
    }

    private void castCatoblepasUntilTriggerPending(Permanent target) {
        harness.setHand(player1, List.of(new BlightBreathCatoblepas()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
