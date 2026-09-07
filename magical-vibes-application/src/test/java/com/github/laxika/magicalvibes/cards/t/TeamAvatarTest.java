package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TeamAvatar.class, GrizzlyBears.class, HillGiant.class})
class TeamAvatarTest extends BaseCardTest {

    @Test
    void loneAttackerGetsBoostBasedOnControlledCreatures() {
        harness.addToBattlefield(player1, new TeamAvatar());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(2));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    void doesNotTriggerWhenMoreThanOneCreatureAttacks() {
        harness.addToBattlefield(player1, new TeamAvatar());
        Permanent firstAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2));

        assertThat(gqs.getEffectivePower(gd, firstAttacker)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondAttacker)).isEqualTo(2);
    }

    @Test
    void handAbilityDealsDamageBasedOnControlledCreaturesAndDiscardsThisCard() {
        harness.setHand(player1, List.of(new TeamAvatar()));
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Team Avatar");
    }

    @Test
    void handAbilityCannotTargetNoncreature() {
        harness.setHand(player1, List.of(new TeamAvatar()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new TeamAvatar());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
