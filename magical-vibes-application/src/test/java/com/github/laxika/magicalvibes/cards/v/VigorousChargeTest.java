package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorosSwiftblade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.y.YavimayaBarbarian;
import com.github.laxika.magicalvibes.cards.z.Zap;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VigorousCharge.class, YavimayaBarbarian.class, Forest.class, Zap.class})
class VigorousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to the target creature")
    void grantsTrample() {
        Permanent target = addCreatureReady(player1, new YavimayaBarbarian());
        castCharge(target, false);

        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("A kicked charge gains life equal to combat damage dealt by the target")
    void kickedChargeGainsLifeEqualToCombatDamage() {
        Permanent target = addCreatureReady(player1, new YavimayaBarbarian());
        castCharge(target, true);
        harness.setLife(player1, 10);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @CardUsed(BorosSwiftblade.class)
    @DisplayName("A kicked charge gains life for each combat damage event")
    void kickedChargeTriggersForEachCombatDamageEvent() {
        Permanent target = addCreatureReady(player1, new BorosSwiftblade());
        castCharge(target, true);
        harness.setLife(player1, 10);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("The kicked trigger still works if the target dies after dealing combat damage")
    void kickedTriggerWorksWhenTargetDiesInCombat() {
        Permanent target = addCreatureReady(player1, new YavimayaBarbarian());
        Permanent blocker = addCreatureReady(player2, new YavimayaBarbarian());
        castCharge(target, true);
        harness.setLife(player1, 10);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        harness.assertInGraveyard(player1, "Yavimaya Barbarian");
    }

    @Test
    @DisplayName("An unkicked charge does not grant the life-gain trigger")
    void unkickedChargeDoesNotGainLife() {
        Permanent target = addCreatureReady(player1, new YavimayaBarbarian());
        castCharge(target, false);
        harness.setLife(player1, 10);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A kicked charge does not trigger on noncombat damage")
    void kickedChargeDoesNotTriggerOnNoncombatDamage() {
        Permanent target = addCreatureReady(player1, new YavimayaBarbarian());
        castCharge(target, true);
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new VigorousCharge()));
        harness.setHand(player1, List.of(new Zap()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("The temporary effects wear off at end of turn")
    void temporaryEffectsWearOff() {
        Permanent target = addCreatureReady(player1, new YavimayaBarbarian());
        castCharge(target, true);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new YavimayaBarbarian());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new VigorousCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID forestId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCharge(Permanent target, boolean kicked) {
        harness.setHand(player1, List.of(new VigorousCharge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
        harness.passBothPriorities();
    }

}
