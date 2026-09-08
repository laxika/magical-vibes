package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelicarrierStrike.class, CrawWurm.class, GrizzlyBears.class})
class HelicarrierStrikeTest extends BaseCardTest {

    @Test
    void dealsTwoDamageToAnAttackingCreatureWithoutTeamwork() {
        Permanent target = addCombatCreature(player2, new CrawWurm(), true);

        castAt(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void dealsFourDamageToABlockingCreatureWhenTeamworkIsPaid() {
        Permanent target = addCombatCreature(player2, new CrawWurm(), false);
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HelicarrierStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstantWithSacrifices(player1, 0, target.getId(), List.of(teammate.getId()));
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(teammate.isTapped()).isTrue();
    }

    @Test
    void cannotTargetANoncombatCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HelicarrierStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new HelicarrierStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addCombatCreature(Player owner, Card card, boolean attacking) {
        harness.addToBattlefield(owner, card);
        Permanent permanent = gd.playerBattlefields.get(owner.getId()).getLast();
        permanent.setSummoningSick(false);
        if (attacking) {
            permanent.setAttacking(true);
            permanent.setAttackTarget(player1.getId());
        } else {
            permanent.setBlocking(true);
            permanent.addBlockingTargetId(UUID.randomUUID());
        }
        return permanent;
    }
}
