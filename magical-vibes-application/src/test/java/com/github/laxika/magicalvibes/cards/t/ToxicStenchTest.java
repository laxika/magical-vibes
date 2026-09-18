package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AnuridBarkripper;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.cards.s.SpiritCairn;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({ToxicStench.class, AnuridBarkripper.class, TreacherousVampire.class,
        SpiritCairn.class, MentalNote.class})
class ToxicStenchTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target nonblack creature -1/-1 without threshold")
    void givesMinusOneMinusOneWithoutThreshold() {
        Permanent target = addCreature();

        castAndResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Keeps the -1/-1 effect at exactly six graveyard cards")
    void keepsMinusOneMinusOneAtSixCards() {
        Permanent target = addCreature();
        setGraveyardSize(6);

        castAndResolve(target);

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        harness.assertOnBattlefield(player2, "Anurid Barkripper");
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void minusOneMinusOneWearsOffAtEndOfTurn() {
        Permanent target = addCreature();

        castAndResolve(target);
        assertThat(target.getPowerModifier()).isEqualTo(-1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Destroys the target without regeneration with threshold")
    void destroysTargetWithoutRegenerationWithThreshold() {
        Permanent target = addCreature();
        target.setRegenerationShield(1);
        setGraveyardSize(7);

        castAndResolve(target);

        harness.assertNotOnBattlefield(player2, "Anurid Barkripper");
        harness.assertInGraveyard(player2, "Anurid Barkripper");
    }

    @Test
    @DisplayName("Checks threshold as the spell resolves")
    void checksThresholdAtResolution() {
        Permanent target = addCreature();
        setGraveyardSize(6);

        cast(target);
        gd.playerGraveyards.get(player1.getId()).add(new MentalNote());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Anurid Barkripper");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TreacherousVampire());
        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpiritCairn());
        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player2, new AnuridBarkripper());
    }

    private void castAndResolve(Permanent target) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void cast(Permanent target) {
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void setGraveyardSize(int size) {
        harness.setGraveyard(player1, List.<Card>of(
                new MentalNote(), new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote()
        ).subList(0, size));
    }
}
