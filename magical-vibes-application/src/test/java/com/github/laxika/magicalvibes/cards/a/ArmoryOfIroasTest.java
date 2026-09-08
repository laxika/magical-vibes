package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArmoryOfIroasTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets a +1/+1 counter when it attacks")
    void equippedCreatureGetsCounterWhenAttacking() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent armory = addReady(player1, new ArmoryOfIroas());
        armory.setAttachedTo(creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No counter is put on an attacking creature when Armory of Iroas is unattached")
    void unattachedArmoryDoesNotTrigger() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        addReady(player1, new ArmoryOfIroas());

        declareAttackers(player1, List.of(0));

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Armory of Iroas"));
    }

    @Test
    @DisplayName("Equip {2} attaches Armory of Iroas to a creature you control")
    void equipAttachesArmory() {
        Permanent armory = addReady(player1, new ArmoryOfIroas());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(armory.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
