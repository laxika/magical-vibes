package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScaldingSalamanderTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger damages defending creatures without flying")
    void damagesDefendingCreaturesWithoutFlying() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());
        Permanent defendingElemental = addCreatureReady(player2, new AirElemental());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(defendingBears.getMarkedDamage()).isEqualTo(1);
        assertThat(defendingElemental.getMarkedDamage()).isZero();
        assertThat(ownBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger deals no damage")
    void decliningAttackTriggerDealsNoDamage() {
        addCreatureReady(player1, new ScaldingSalamander());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingBears.getMarkedDamage()).isZero();
    }
}
