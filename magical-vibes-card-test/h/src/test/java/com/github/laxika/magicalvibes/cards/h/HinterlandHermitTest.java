package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HinterlandHermit.class, HinterlandScourge.class, DawntreaderElk.class})
class HinterlandHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Hinterland Scourge when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(hermit.isTransformed()).isTrue();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Scourge");
        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hermit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        assertThat(hermit.isTransformed()).isFalse();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Hermit");
    }

    @Test
    @DisplayName("Hinterland Scourge transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(hermit.isTransformed()).isFalse();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Hermit");
        assertThat(gqs.getEffectivePower(gd, hermit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hermit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Hinterland Scourge does not transform back when no player cast two spells last turn")
    void doesNotTransformBackWithOnlyOneSpellCastLastTurn() {
        harness.addToBattlefield(player1, new HinterlandHermit());
        Permanent hermit = findPermanent(player1, "Hinterland Hermit");

        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        assertThat(hermit.isTransformed()).isTrue();
        assertThat(hermit.getCard().getName()).isEqualTo("Hinterland Scourge");
    }

    @Test
    @DisplayName("Hinterland Scourge must be blocked if able")
    void hinterlandScourgeMustBeBlockedIfAble() {
        Permanent scourge = addCreatureReady(player1, new HinterlandScourge());
        scourge.setAttacking(true);
        addCreatureReady(player2, new DawntreaderElk());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("One blocker satisfies Hinterland Scourge's blocking requirement")
    void oneBlockerSatisfiesHinterlandScourgeRequirement() {
        Permanent scourge = addCreatureReady(player1, new HinterlandScourge());
        scourge.setAttacking(true);
        addCreatureReady(player2, new DawntreaderElk());
        addCreatureReady(player2, new DawntreaderElk());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId()).get(1).isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block Hinterland Scourge")
    void tappedCreaturesAreNotForcedToBlockHinterlandScourge() {
        Permanent scourge = addCreatureReady(player1, new HinterlandScourge());
        scourge.setAttacking(true);
        Permanent tapped = addCreatureReady(player2, new DawntreaderElk());
        tapped.tap();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Hermit transforms during the opponent's upkeep after a spell-free turn")
    void transformsOnOpponentsUpkeep() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new HinterlandHermit());
        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(hermit.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "Hinterland Scourge");
    }

    @Test
    @DisplayName("An opponent's spell also prevents the Hermit upkeep trigger")
    void opponentsSpellPreventsTransformation() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new HinterlandHermit());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(hermit.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Scourge transforms back when its controller cast more than two spells")
    void controllersThreeSpellsTransformScourgeBack() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new HinterlandHermit());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 3);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(hermit.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Hinterland Hermit");
    }

    @Test
    @DisplayName("A spell-free turn leaves Scourge transformed without an upkeep trigger")
    void scourgeStaysTransformedAfterSpellFreeTurn() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new HinterlandHermit());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(hermit.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Hermit's front face has no blocking requirement")
    void frontFaceMayBeLeftUnblocked() {
        Permanent hermit = addCreatureReady(player1, new HinterlandHermit());
        hermit.setAttacking(true);
        addCreatureReady(player2, new DawntreaderElk());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("Scourge can attack unblocked when there are no defending creatures")
    void noCreaturesMeansScourgeCannotBeBlocked() {
        Permanent scourge = addCreatureReady(player1, new HinterlandScourge());
        scourge.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("An available blocker cannot block another attacker instead of Scourge")
    void blockingAnotherAttackerDoesNotSatisfyScourgeRequirement() {
        Permanent scourge = addCreatureReady(player1, new HinterlandScourge());
        scourge.setAttacking(true);
        Permanent elk = addCreatureReady(player1, new DawntreaderElk());
        elk.setAttacking(true);
        addCreatureReady(player2, new DawntreaderElk());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Transforming Hermit grants the Scourge blocking requirement")
    void transformationGrantsBlockingRequirement() {
        Permanent hermit = addCreatureReady(player1, new HinterlandHermit());
        addCreatureReady(player2, new DawntreaderElk());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isTrue();
        hermit.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");
    }

    @Test
    @DisplayName("Transforming back removes the Scourge blocking requirement")
    void transformingBackRemovesBlockingRequirement() {
        Permanent hermit = addCreatureReady(player1, new HinterlandHermit());
        addCreatureReady(player2, new DawntreaderElk());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.isTransformed()).isFalse();
        hermit.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
    }
}
