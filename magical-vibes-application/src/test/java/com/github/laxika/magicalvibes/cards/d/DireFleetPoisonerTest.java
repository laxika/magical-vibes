package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({DireFleetPoisoner.class, DireFleetCaptain.class, RaptorCompanion.class, SailorOfMeans.class})
class DireFleetPoisonerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an attacking Pirate +1/+1 and deathtouch")
    void etbBoostsAttackingPirateAndGrantsDeathtouch() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());

        resolveAllTriggers();

        assertThat(pirate.getEffectivePower()).isEqualTo(3);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(3);
        assertThat(pirate.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("ETB boost and deathtouch wear off at end of turn")
    void etbEffectsWearOffAtEndOfTurn() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(pirate.getEffectivePower()).isEqualTo(2);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(2);
        assertThat(pirate.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Cannot target a Pirate that is not attacking")
    void cannotTargetNonAttackingPirate() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        harness.setHand(player1, List.of(new DireFleetPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, pirate.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Pirate you control");
    }

    @Test
    @DisplayName("Cannot target an attacking creature an opponent controls")
    void cannotTargetOpponentAttackingPirate() {
        Permanent pirate = addCreatureReady(player2, new DireFleetCaptain());
        pirate.setAttacking(true);
        harness.setHand(player1, List.of(new DireFleetPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, pirate.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Pirate you control");
    }

    @Test
    @DisplayName("Cannot target an attacking non-Pirate")
    void cannotTargetAttackingNonPirate() {
        Permanent dinosaur = addCreatureReady(player1, new RaptorCompanion());
        dinosaur.setAttacking(true);
        harness.setHand(player1, List.of(new DireFleetPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, dinosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking Pirate you control");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat without a legal ETB target")
    void canCastDuringOpponentCombatWithoutLegalTarget() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castFromHand(player1, new DireFleetPoisoner(), "{1}{B}");

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getClass())
                .containsExactly(DireFleetPoisoner.class);
    }

    @Test
    @DisplayName("ETB does not affect a Pirate that stops attacking before resolution")
    void targetMustStillBeAttackingAtResolution() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        pirate.setAttacking(false);

        resolveAllTriggers();

        assertThat(pirate.getEffectivePower()).isEqualTo(2);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(2);
        assertThat(pirate.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB does not affect a Pirate whose controller changes before resolution")
    void targetMustStillBeControlledAtResolution() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(pirate);
        gd.playerBattlefields.get(player2.getId()).add(pirate);

        resolveAllTriggers();

        assertThat(pirate.getEffectivePower()).isEqualTo(2);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(2);
        assertThat(pirate.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB resolves independently of Dire Fleet Poisoner remaining on the battlefield")
    void etbResolvesAfterSourceLeavesBattlefield() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent poisoner = findPermanent(player1, "Dire Fleet Poisoner");
        gd.playerBattlefields.get(player1.getId()).remove(poisoner);
        gd.playerGraveyards.get(player1.getId()).add(poisoner.getCard());

        resolveAllTriggers();

        assertThat(pirate.getEffectivePower()).isEqualTo(3);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(3);
        assertThat(pirate.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Resolved ETB bonuses remain after the Pirate stops attacking")
    void resolvedBonusesDoNotRequireContinuedAttacking() {
        Permanent pirate = addCreatureReady(player1, new DireFleetCaptain());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());
        resolveAllTriggers();

        pirate.setAttacking(false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThat(pirate.getEffectivePower()).isEqualTo(3);
        assertThat(pirate.getEffectiveToughness()).isEqualTo(3);
        assertThat(pirate.getGrantedKeywords()).contains(Keyword.DEATHTOUCH);
    }

    @Test
    @DisplayName("Dire Fleet Poisoner's own deathtouch destroys a tougher blocker")
    void nativeDeathtouchKillsTougherBlocker() {
        addCreatureReady(player1, new DireFleetPoisoner());
        addCreatureReady(player2, new SailorOfMeans());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Sailor of Means");
        harness.assertOnBattlefield(player1, "Dire Fleet Poisoner");
    }

    @Test
    @DisplayName("Granted deathtouch destroys a blocker despite insufficient ordinary damage")
    void grantedDeathtouchKillsTougherBlocker() {
        Permanent pirate = addCreatureReady(player1, new SailorOfMeans());
        addCreatureReady(player2, new SailorOfMeans());
        pirate.setAttacking(true);
        castPoisoner(pirate.getId());
        resolveAllTriggers();
        pirate.setAttacking(false);

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Sailor of Means");
        harness.assertOnBattlefield(player1, "Sailor of Means");
    }

    private void castPoisoner(UUID targetId) {
        harness.setHand(player1, List.of(new DireFleetPoisoner()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gs.playCard(gd, player1, 0, 0, targetId, null);
    }
}
