package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.FleetwheelCruiser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({SavagePunch.class, AlpineGrizzly.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, FleetwheelCruiser.class})
class SavagePunchTest extends BaseCardTest {

    @Test
    @DisplayName("Ferocious boosts the creature before it fights")
    void ferociousBoostsBeforeFight() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new HillGiant());
        castSavagePunch("Grizzly Bears", "Hill Giant");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Without ferocious, the creature fights without a boost")
    void fightsWithoutFerocious() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        castSavagePunch("Grizzly Bears", "Hill Giant");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The ferocious boost wears off at end of turn")
    void ferociousBoostWearsOff() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new LlanowarElves());
        castSavagePunch("Grizzly Bears", "Llanowar Elves");

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature as the first target")
    void cannotTargetOpponentCreatureFirst() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SavagePunch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID opponentGiantId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentBearsId, opponentGiantId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target your own creature as the second target")
    void cannotTargetOwnCreatureSecond() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new SavagePunch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, giantId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An uncrewed Vehicle does not satisfy ferocious")
    void noncreatureVehicleDoesNotEnableFerocious() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FleetwheelCruiser());
        harness.addToBattlefield(player2, new HillGiant());

        castSavagePunch("Grizzly Bears", "Hill Giant");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("The fighting creature itself can satisfy ferocious")
    void fightingCreatureEnablesFerocious() {
        Permanent grizzly = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new HillGiant());

        castSavagePunch("Alpine Grizzly", "Hill Giant");

        harness.assertOnBattlefield(player1, "Alpine Grizzly");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, grizzly)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, grizzly)).isEqualTo(4);
    }

    @Test
    @DisplayName("Losing the ferocious enabler before resolution prevents the boost")
    void ferociousCheckedAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent enabler = harness.addToBattlefieldAndReturn(player1, new AlpineGrizzly());
        harness.addToBattlefield(player2, new HillGiant());
        putSavagePunchOnStack("Grizzly Bears", "Hill Giant");

        gd.playerBattlefields.get(player1.getId()).remove(enabler);
        gd.playerHands.get(player1.getId()).add(enabler.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("An absent opposing target prevents the fight but not the ferocious boost")
    void missingOpponentStillAllowsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        putSavagePunchOnStack("Grizzly Bears", "Hill Giant");

        gd.playerBattlefields.get(player2.getId()).remove(giant);
        gd.playerHands.get(player2.getId()).add(giant.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent's powerful creature does not enable ferocious")
    void opponentsCreatureDoesNotEnableFerocious() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new AlpineGrizzly());

        castSavagePunch("Grizzly Bears", "Hill Giant");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("An illegal friendly target receives no boost and neither creature fights")
    void friendlyTargetChangesControllerBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AlpineGrizzly());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        putSavagePunchOnStack("Grizzly Bears", "Hill Giant");

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(giant.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
    }

    private void castSavagePunch(String firstTargetName, String secondTargetName) {
        putSavagePunchOnStack(firstTargetName, secondTargetName);
        harness.passBothPriorities();
    }

    private void putSavagePunchOnStack(String firstTargetName, String secondTargetName) {
        harness.setHand(player1, List.of(new SavagePunch()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID firstTargetId = harness.getPermanentId(player1, firstTargetName);
        UUID secondTargetId = harness.getPermanentId(player2, secondTargetName);
        harness.castSorcery(player1, 0, List.of(firstTargetId, secondTargetId));
    }
}
