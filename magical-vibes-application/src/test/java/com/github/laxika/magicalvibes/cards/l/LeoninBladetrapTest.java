package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.s.SomberHoverguard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninBladetrap.class, Cathodion.class, Frogmite.class, SomberHoverguard.class})
class LeoninBladetrapTest extends BaseCardTest {

    @Test
    void damagesAttackingCreaturesWithoutFlyingAndSacrificesItself() {
        harness.addToBattlefield(player1, new LeoninBladetrap());

        Permanent attackingLethalGroundCreature = addCreatureReady(player2, new Frogmite());
        attackingLethalGroundCreature.setAttacking(true);

        Permanent attackingGroundCreature = addCreatureReady(player2, new Cathodion());
        attackingGroundCreature.setAttacking(true);

        Permanent attackingFlyingCreature = addCreatureReady(player2, new SomberHoverguard());
        attackingFlyingCreature.setAttacking(true);

        Permanent nonAttackingGroundCreature = addCreatureReady(player2, new Cathodion());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Leonin Bladetrap");
        harness.assertInGraveyard(player1, "Leonin Bladetrap");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attackingLethalGroundCreature);
        assertThat(attackingGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(attackingFlyingCreature.getMarkedDamage()).isZero();
        assertThat(nonAttackingGroundCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Somber Hoverguard");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonAttackingGroundCreature);
    }

    @Test
    @DisplayName("Flash allows Leonin Bladetrap to be cast during an opponent's combat")
    void flashAllowsCastingDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new LeoninBladetrap()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.passPriority(player2);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leonin Bladetrap");
    }
}
