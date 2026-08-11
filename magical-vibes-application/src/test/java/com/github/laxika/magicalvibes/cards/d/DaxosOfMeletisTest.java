package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaxosOfMeletisTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the damaged player's top card and gains its mana value in life")
    void combatDamageExilesTopCardAndGainsLife() {
        addAttackingDaxos(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());
    }

    @Test
    @DisplayName("The exiled spell can be cast with mana of any color until end of turn")
    void castsExiledSpellWithManaOfAnyColor() {
        addAttackingDaxos(player1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A land exiled by the trigger cannot be played")
    void doesNotGrantLandPlayPermission() {
        addAttackingDaxos(player1);
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard, new GrizzlyBears()));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Daxos cannot be blocked by a creature with power 3 or greater")
    void cannotBeBlockedByPowerThreeOrGreater() {
        Permanent daxos = addAttackingDaxos(player1);
        Permanent blocker = addCreatureReady(player2, new HillGiant());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int daxosIndex = gd.playerBattlefields.get(player1.getId()).indexOf(daxos);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, daxosIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackingDaxos(Player player) {
        Permanent daxos = addCreatureReady(player, new DaxosOfMeletis());
        daxos.setAttacking(true);
        return daxos;
    }

    private void resolveCombatAndTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
