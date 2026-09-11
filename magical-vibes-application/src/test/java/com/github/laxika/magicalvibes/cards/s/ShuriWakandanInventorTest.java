package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UrzasSylex;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardSupertype;
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

@CardUsed({ShuriWakandanInventor.class, GrizzlyBears.class, UrzasSylex.class, WornPowerstone.class})
class ShuriWakandanInventorTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact spells you cast cost {1} less")
    void reducesArtifactSpellCost() {
        harness.addToBattlefield(player1, new ShuriWakandanInventor());
        harness.setHand(player1, List.of(new WornPowerstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Worn Powerstone")).isNotNull();
    }

    @Test
    @DisplayName("The cost reduction does not apply to nonartifact spells")
    void doesNotReduceNonartifactSpellCost() {
        harness.addToBattlefield(player1, new ShuriWakandanInventor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The copy ability removes legendary until end of turn")
    void copiesArtifactWithoutLegendaryUntilEndOfTurn() {
        Permanent shuri = addReadyShuri();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent copySource = harness.addToBattlefieldAndReturn(player1, new UrzasSylex());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, indexOf(shuri), 0,
                List.of(target.getId(), copySource.getId()));
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Urza's Sylex");
        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.LEGENDARY)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Worn Powerstone");
    }

    @Test
    @DisplayName("The copy ability may use the same artifact as both targets")
    void allowsSharedTargets() {
        Permanent shuri = addReadyShuri();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UrzasSylex());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, indexOf(shuri), 0,
                List.of(target.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, target, CardSupertype.LEGENDARY)).isFalse();
    }

    @Test
    @DisplayName("The copy ability cannot target an artifact an opponent controls")
    void requiresArtifactsYouControl() {
        Permanent shuri = addReadyShuri();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new WornPowerstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, indexOf(shuri), 0,
                List.of(opponentArtifact.getId(), ownArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact you control");
    }

    @Test
    @DisplayName("The copy ability can be activated only at sorcery speed")
    void requiresSorcerySpeed() {
        Permanent shuri = addReadyShuri();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        Permanent copySource = harness.addToBattlefieldAndReturn(player1, new WornPowerstone());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, indexOf(shuri), 0,
                List.of(target.getId(), copySource.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyShuri() {
        Permanent shuri = harness.addToBattlefieldAndReturn(player1, new ShuriWakandanInventor());
        shuri.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return shuri;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
