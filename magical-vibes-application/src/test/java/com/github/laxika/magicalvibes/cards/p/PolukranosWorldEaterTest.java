package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PolukranosWorldEaterTest extends BaseCardTest {

    @Test
    void monstrosityUsesPaidXAndResolvesTheDividedDamageAtResolution() {
        Permanent polukranos = addReadyPolukranos(player1);
        Permanent firstBears = addReadyCreature(player2);
        Permanent secondBears = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, firstBears.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, secondBears.getId());

        harness.passBothPriorities();
        PendingInteraction.XValueChoice allocation =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(allocation).isNotNull();
        assertThat(allocation.minValue()).isEqualTo(1);
        assertThat(allocation.maxValue()).isEqualTo(1);

        harness.handleXValueChosen(player1, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 1);

        assertThat(polukranos.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(polukranos.isMonstrous()).isTrue();
        assertThat(polukranos.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(firstBears.getId())
                        && permanent.getMarkedDamage() == 1)
                .anyMatch(permanent -> permanent.getId().equals(secondBears.getId())
                        && permanent.getMarkedDamage() == 1);
    }

    private Permanent addReadyPolukranos(Player player) {
        Permanent permanent = new Permanent(new PolukranosWorldEater());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
