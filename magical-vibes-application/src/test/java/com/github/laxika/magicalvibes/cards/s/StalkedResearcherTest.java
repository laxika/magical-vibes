package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StalkedResearcher.class, GloriousAnthem.class, DazzlingTheaterPropRoom.class, GrizzlyBears.class})
class StalkedResearcherTest extends BaseCardTest {

    @Test
    void canAttackAfterAnEnchantmentYouControlEnters() {
        Permanent researcher = addResearcher();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        declareResearcherAttack(researcher);

        assertThat(researcher.isAttacking()).isTrue();
    }

    @Test
    void canAttackAfterYouFullyUnlockARoom() {
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        Permanent researcher = addResearcher();
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 0, 1);
        harness.passBothPriorities();

        declareResearcherAttack(researcher);

        assertThat(researcher.isAttacking()).isTrue();
    }

    @Test
    void doesNotTriggerForAnOpponentsEnchantment() {
        Permanent researcher = addResearcher();
        researcher.setSummoningSick(false);
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareResearcherAttack(researcher))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void attackPermissionExpiresAtEndOfTurn() {
        Permanent researcher = addResearcher();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        researcher.setSummoningSick(false);

        assertThatThrownBy(() -> declareResearcherAttack(researcher))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addResearcher() {
        return harness.addToBattlefieldAndReturn(player1, new StalkedResearcher());
    }

    private void declareResearcherAttack(Permanent researcher) {
        harness.addToBattlefield(player2, new GrizzlyBears());
        researcher.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));

        int researcherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(researcher);
        gs.declareAttackers(gd, player1, List.of(researcherIndex));
    }
}
