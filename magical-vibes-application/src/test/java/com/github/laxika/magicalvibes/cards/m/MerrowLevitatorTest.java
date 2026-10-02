package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LeeringEmblem;
import com.github.laxika.magicalvibes.cards.o.OdiousTrow;
import com.github.laxika.magicalvibes.cards.s.StreamHopper;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({MerrowLevitator.class, StreamHopper.class, OdiousTrow.class, LeeringEmblem.class})
class MerrowLevitatorTest extends BaseCardTest {

    @Test
    @DisplayName("{T} ability grants flying to the target creature")
    void grantsFlyingToTarget() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        Permanent target = addCreatureReady(player2, new StreamHopper());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(levitator.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("{T} ability targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new MerrowLevitator());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new LeeringEmblem());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying granted by the {T} ability wears off at end of turn")
    void flyingExpiresAtEndOfTurn() {
        addCreatureReady(player1, new MerrowLevitator());
        Permanent target = addCreatureReady(player1, new StreamHopper());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Casting a blue spell lets the controller untap Merrow Levitator")
    void untapsWhenCastingBlueSpell() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();
        harness.setHand(player1, List.of(new StreamHopper()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(levitator.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the may ability leaves Merrow Levitator tapped")
    void staysTappedWhenDeclining() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();
        harness.setHand(player1, List.of(new StreamHopper()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(levitator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-blue spell does not trigger the untap")
    void nonBlueSpellDoesNotTrigger() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();
        harness.setHand(player1, List.of(new OdiousTrow()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(levitator.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's blue spell does not trigger the untap")
    void opponentBlueSpellDoesNotTrigger() {
        Permanent levitator = addCreatureReady(player1, new MerrowLevitator());
        levitator.tap();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new StreamHopper()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(levitator.isTapped()).isTrue();
    }
}
