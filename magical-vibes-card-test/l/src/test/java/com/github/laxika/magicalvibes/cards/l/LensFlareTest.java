package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LensFlare.class, Spellbook.class, HillGiant.class, GrizzlyBears.class, LandscaperColos.class})
class LensFlareTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for artifacts reduces the generic mana cost")
    void affinityForArtifactsReducesGenericCost() {
        addArtifacts(player1, 4);
        Permanent target = addAttacker(new HillGiant());
        prepareCast();

        harness.castInstant(player1, 0, target.getId());

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).hasSize(1);
        assertThat(gameData.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Deals 5 damage to an attacking creature")
    void dealsFiveDamageToAttackingCreature() {
        addArtifacts(player1, 4);
        Permanent target = addAttacker(new HillGiant());
        prepareCast();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addArtifacts(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    void dealsExactlyFiveDamageToBlockingCreature() {
        addArtifacts(player1, 4);
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LandscaperColos());
        target.setBlocking(true);
        target.getBlockingTargets().add(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        prepareCast();
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Landscaper Colos");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player2, 20);
    }

    @Test
    void paysFullCostWithoutArtifacts() {
        Permanent target = addAttacker(new HillGiant());
        prepareCast();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opposingArtifactsDoNotReduceCost() {
        addArtifacts(player2, 4);
        Permanent target = addAttacker(new HillGiant());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Lens Flare");
    }

    @Test
    void excessAffinityCannotPayWhiteCost() {
        addArtifacts(player1, 6);
        Permanent target = addAttacker(new HillGiant());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LensFlare()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Lens Flare");
    }

    @Test
    void excessAffinityStillAllowsCastingForOneWhite() {
        addArtifacts(player1, 6);
        Permanent target = addAttacker(new HillGiant());
        prepareCast();

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotDamageTargetThatStopsAttackingBeforeResolution() {
        addArtifacts(player1, 4);
        Permanent target = addAttacker(new HillGiant());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        target.setAttacking(false);
        target.setAttackTarget(null);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Lens Flare");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareCast() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LensFlare()));
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, card);
        target.setSummoningSick(false);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        return target;
    }

    private void addArtifacts(com.github.laxika.magicalvibes.model.Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Spellbook());
        }
    }
}
