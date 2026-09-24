package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AnuridBarkripper;
import com.github.laxika.magicalvibes.cards.c.CabalTrainee;
import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
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

@CardUsed({AnuridBarkripper.class, CabalTrainee.class, GiantWarthog.class, KrosanVerge.class, MentalNote.class, ToxicStench.class})
class ToxicStenchTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a target nonblack creature -1/-1 without threshold")
    void givesMinusOneMinusOneWithoutThreshold() {
        Permanent target = addCreatureReady(player2, new GiantWarthog());

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Destroys the target without regeneration with threshold")
    void destroysTargetWithoutRegenerationWithThreshold() {
        Permanent target = addCreatureReady(player2, new GiantWarthog());
        target.setRegenerationShield(1);
        setGraveyardSize(7);

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Giant Warthog");
        harness.assertInGraveyard(player2, "Giant Warthog");
    }

    @Test
    @DisplayName("Checks threshold as the spell resolves")
    void checksThresholdAtResolution() {
        Permanent target = addCreatureReady(player2, new GiantWarthog());
        setGraveyardSize(6);

        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).add(new GiantWarthog());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Warthog");
    }

    @Test
    @DisplayName("Uses the non-threshold effect when threshold is lost before resolution")
    void usesMinusOneMinusOneWhenThresholdIsLostBeforeResolution() {
        Permanent target = addCreatureReady(player2, new GiantWarthog());
        setGraveyardSize(7);

        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Giant Warthog");
    }

    @Test
    @DisplayName("The -1/-1 effect wears off at end of turn")
    void minusOneMinusOneWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GiantWarthog());

        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CabalTrainee());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ToxicStench()));
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void setGraveyardSize(int size) {
        harness.setGraveyard(player1, List.<Card>of(
                new GiantWarthog(), new GiantWarthog(), new GiantWarthog(), new GiantWarthog(),
                new GiantWarthog(), new GiantWarthog(), new GiantWarthog()
        ).subList(0, size));
    }

    @Test
    @DisplayName("Keeps the -1/-1 effect at exactly six graveyard cards")
    void keepsMinusOneMinusOneAtSixCards() {
        Permanent target = addCreatureForJudReview();
        setGraveyardSizeForJudReview(6);

        castAndResolveForJudReview(target);

        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
        harness.assertOnBattlefield(player2, "Anurid Barkripper");
    }

    private Permanent addCreatureForJudReview() {
        return harness.addToBattlefieldAndReturn(player2, new AnuridBarkripper());
    }

    private void castAndResolveForJudReview(Permanent target) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void setGraveyardSizeForJudReview(int size) {
        harness.setGraveyard(player1, List.<Card>of(
                new MentalNote(), new MentalNote(), new MentalNote(), new MentalNote(),
                new MentalNote(), new MentalNote(), new MentalNote()
        ).subList(0, size));
    }
}
