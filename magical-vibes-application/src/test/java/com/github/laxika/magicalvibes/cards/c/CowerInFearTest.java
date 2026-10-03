package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CowerInFear.class, GrizzlyBears.class, FugitiveWizard.class})
class CowerInFearTest extends BaseCardTest {

    @Test
    @DisplayName("Gives creatures opponents control -1/-1 and leaves your own creatures alone")
    void weakensOnlyOpponentCreatures() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CowerInFear()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's 1/1 dies to the -1/-1")
    void killsOneToughnessOpponentCreatures() {
        addCreatureReady(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new CowerInFear()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("The -1/-1 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent enemyBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CowerInFear()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution are not weakened")
    void doesNotWeakenCreaturesEnteringLater() {
        Permanent existingBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CowerInFear()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        Permanent laterBear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, existingBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, existingBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering while the spell is on the stack are weakened")
    void determinesAffectedCreaturesAtResolution() {
        harness.setHand(player1, List.of(new CowerInFear()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0);
        Permanent enemyBear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyBear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyBear)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves without opposing creatures and spares your own 1/1")
    void resolvesWithoutOpposingCreatures() {
        Permanent ownWizard = addCreatureReady(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new CowerInFear()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Cower in Fear");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        assertThat(gqs.getEffectivePower(gd, ownWizard)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownWizard)).isEqualTo(1);
    }
}
