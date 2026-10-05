package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.j.Jilt;
import com.github.laxika.magicalvibes.cards.t.TundraKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KavuMauler.class, KavuGlider.class, TundraKavu.class, Dodecapod.class, Jilt.class})
class KavuMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("The attack bonus increases the damage that tramples over a blocker")
    void boostedDamageTramplesOverBlocker() {
        addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, new KavuGlider());
        addCreatureReady(player2, new Dodecapod());

        declareAttackersAndPrepareBlockers(player1, List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Dodecapod");
        harness.assertOnBattlefield(player1, "Kavu Mauler");
    }

    @Test
    @DisplayName("Each attacking Mauler counts the other Mauler independently")
    void multipleMaulersBoostIndependently() {
        Permanent first = addCreatureReady(player1, new KavuMauler());
        Permanent second = addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, new KavuGlider());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(first.getPowerModifier()).isEqualTo(2);
        assertThat(first.getToughnessModifier()).isEqualTo(2);
        assertThat(second.getPowerModifier()).isEqualTo(2);
        assertThat(second.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts attacking Kavus at resolution rather than at declaration")
    void excludesKavuReturnedBeforeTriggerResolves() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        Permanent glider = addCreatureReady(player1, new KavuGlider());
        harness.setHand(player1, List.of(new Jilt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            assertThat(gd.stack).hasSize(1);
            harness.castAndResolveInstant(player1, 0, glider.getId());
            resolveAllTriggers();
        });

        harness.assertInHand(player1, "Kavu Glider");
        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Returning another Kavu after resolution does not reduce the bonus")
    void resolvedBonusDoesNotRecalculate() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        Permanent glider = addCreatureReady(player1, new KavuGlider());
        harness.setHand(player1, List.of(new Jilt()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1));
            resolveAllTriggers();
            assertThat(mauler.getPowerModifier()).isEqualTo(1);
            harness.castAndResolveInstant(player1, 0, glider.getId());
        });

        harness.assertInHand(player1, "Kavu Glider");
        assertThat(mauler.getPowerModifier()).isEqualTo(1);
        assertThat(mauler.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +1/+1 for each other attacking Kavu")
    void boostsForOtherAttackingKavus() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, new KavuGlider());
        addCreatureReady(player1, new KavuGlider());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(mauler.getPowerModifier()).isEqualTo(2);
        assertThat(mauler.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a bonus when attacking alone")
    void noBonusWhenAttackingAlone() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Counts only other attacking Kavus")
    void ignoresNonKavuAndNonAttackingCreatures() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, new KavuGlider());
        addCreatureReady(player1, new Dodecapod());
        addCreatureReady(player1, new TundraKavu());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(mauler.getPowerModifier()).isEqualTo(1);
        assertThat(mauler.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The temporary bonus resets at end of turn")
    void bonusResetsAtEndOfTurn() {
        Permanent mauler = addCreatureReady(player1, new KavuMauler());
        addCreatureReady(player1, new KavuGlider());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();
        assertThat(mauler.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(mauler.getPowerModifier()).isZero();
        assertThat(mauler.getToughnessModifier()).isZero();
    }
}
