package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PuppetMasterStringPuller.class, GrizzlyBears.class, HermeticStudy.class})
class PuppetMasterStringPullerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking goads a target opponent creature and stops it blocking this turn")
    void attackingGoadsTargetOpponentCreature() {
        Permanent puppet = addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(gqs.isGoaded(gd, target)).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(bls.canBlockAttacker(gd, target, puppet, List.of(target))).isFalse();
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Combat damage from a goaded creature creates a Treasure")
    void combatDamageFromGoadedCreatureCreatesTreasure() {
        addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        goadTarget(target);

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Noncombat damage from a goaded creature does not create a Treasure")
    void noncombatDamageFromGoadedCreatureDoesNotCreateTreasure() {
        addCreatureReady(player1, new PuppetMasterStringPuller());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        goadTarget(target);

        Permanent study = harness.addToBattlefieldAndReturn(player2, new HermeticStudy());
        study.setAttachedTo(target.getId());
        int studyIndex = gd.playerBattlefields.get(player2.getId()).indexOf(study);
        harness.activateAbility(player2, studyIndex, null, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private void goadTarget(Permanent target) {
        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
    }
}
