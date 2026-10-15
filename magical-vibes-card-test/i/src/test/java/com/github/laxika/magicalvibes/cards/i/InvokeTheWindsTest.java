package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvokeTheWinds.class, FountainOfYouth.class, GrizzlyBears.class, Plains.class, Unsummon.class})
class InvokeTheWindsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains permanent control of and untaps a target creature")
    void gainsControlOfAndUntapsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Gains permanent control of and untaps a target artifact")
    void gainsControlOfAndUntapsArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        target.tap();

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new InvokeTheWinds()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Can target and untap a creature you already control")
    void untapsOwnCreatureWithoutChangingControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setSummoningSick(false);
        target.tap();

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("Control lasts through cleanup and subsequent turns without granting haste")
    void controlDoesNotExpireAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setSummoningSick(false);

        cast(target);

        assertThat(target.isSummoningSick()).isTrue();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("Does not gain control or untap anything when the target leaves before resolution")
    void targetReturnedToHandBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        Permanent other = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        other.tap();
        harness.setHand(player1, List.of(new InvokeTheWinds()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
        assertThat(other.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Invoke the Winds");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new InvokeTheWinds()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 4);
    }
}
