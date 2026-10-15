package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.ArmoredWolfRider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatalFumes.class, GrizzlyBears.class, ArmoredWolfRider.class, KraulWarrior.class})
class FatalFumesTest extends BaseCardTest {

    private void setupOpponentBear() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 4);
    }

    @Test
    @DisplayName("Resolving gives -4/-2 and kills a 2/2")
    void killsGrizzlyBears() {
        setupOpponentBear();
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearId);

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Fatal Fumes");
    }

    @Test
    @DisplayName("A surviving creature keeps -4/-2 until it wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setToughnessModifier(5);

        harness.castAndResolveInstant(player1, 0, bearId);

        assertThat(bear.getEffectivePower()).isEqualTo(-2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(0);
        assertThat(bear.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast with an invalid target")
    void cannotCastWithInvalidTarget() {
        setupOpponentBear();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target");
    }

    @Test
    @DisplayName("A real creature survives with exactly -4/-2 and recovers at cleanup")
    void survivingCreatureRecoversAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredWolfRider());
        harness.setHand(player1, List.of(new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Armored Wolf-Rider");
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    @DisplayName("Two applications accumulate and allow negative power")
    void multipleApplicationsAccumulate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArmoredWolfRider());
        harness.setHand(player1, List.of(new FatalFumes(), new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Armored Wolf-Rider");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing the only target before resolution does not affect another creature")
    void missingTargetDoesNotAffectAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KraulWarrior());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new ArmoredWolfRider());
        harness.setHand(player1, List.of(new FatalFumes(), new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Kraul Warrior");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Fatal Fumes", "Fatal Fumes");
        assertThat(gqs.getEffectivePower(gd, survivor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, survivor)).isEqualTo(6);
    }

    @Test
    @DisplayName("A player is not a legal target")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FatalFumes()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Fatal Fumes");
    }
}
