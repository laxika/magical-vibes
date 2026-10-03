package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ExpeditionEnvoy;
import com.github.laxika.magicalvibes.cards.g.GideonsReproach;
import com.github.laxika.magicalvibes.cards.k.KorBladewhirl;
import com.github.laxika.magicalvibes.cards.s.StoneHavenMedic;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicCaptain.class, KorBladewhirl.class, StoneHavenMedic.class,
        ExpeditionEnvoy.class, GideonsReproach.class})
class AngelicCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other attacking Ally")
    void boostScalesWithOtherAttackingAllies() {
        Permanent captain = addCreatureReady(player1, new AngelicCaptain());
        addCreatureReady(player1, new KorBladewhirl());
        addCreatureReady(player1, new KorBladewhirl());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(2);
        assertThat(captain.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone gives no boost")
    void noBoostWhenAttackingAlone() {
        Permanent captain = addCreatureReady(player1, new AngelicCaptain());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isZero();
        assertThat(captain.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts only other attacking Allies")
    void ignoresNonAlliesAndNonAttackingAllies() {
        Permanent captain = addCreatureReady(player1, new AngelicCaptain());
        addCreatureReady(player1, new KorBladewhirl());
        addCreatureReady(player1, new KorBladewhirl());
        addCreatureReady(player1, new StoneHavenMedic());

        declareAttackers(List.of(0, 1, 3));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("An Ally removed before resolution is not counted")
    void countsAlliesAtResolution() {
        Permanent captain = addCreatureReady(player1, new AngelicCaptain());
        Permanent ally = addCreatureReady(player1, new ExpeditionEnvoy());
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player2, 0, ally.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Expedition Envoy");
        assertThat(captain.getPowerModifier()).isZero();
        assertThat(captain.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Resolved boost stays fixed after an Ally dies and expires at cleanup")
    void boostStaysFixedUntilEndOfTurn() {
        Permanent captain = addCreatureReady(player1, new AngelicCaptain());
        Permanent ally = addCreatureReady(player1, new ExpeditionEnvoy());
        harness.setHand(player2, List.of(new GideonsReproach()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);

        harness.castInstant(player2, 0, ally.getId());
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Expedition Envoy");
        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);

        harness.passUntil(TurnStep.END_STEP);
        assertThat(captain.getPowerModifier()).isEqualTo(1);
        assertThat(captain.getToughnessModifier()).isEqualTo(1);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(captain.getPowerModifier()).isZero();
        assertThat(captain.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Other attacking Allies do not trigger a Captain that stays back")
    void doesNotTriggerUnlessCaptainAttacks() {
        Permanent captain = addCreatureReady(player1, new AngelicCaptain());
        addCreatureReady(player1, new ExpeditionEnvoy());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(captain.getPowerModifier()).isZero();
        assertThat(captain.getToughnessModifier()).isZero();
    }
}
