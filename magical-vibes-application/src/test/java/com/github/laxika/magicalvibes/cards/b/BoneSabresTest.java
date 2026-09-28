package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneSabres.class, GrizzlyBears.class})
class BoneSabresTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsFourCountersWhenAttacking() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sabres = addReady(player1, new BoneSabres());
        sabres.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void unattachedSabresDoesNotPutCountersOnAttackingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReady(player1, new BoneSabres());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void equipAttachesBoneSabresToCreatureYouControl() {
        Permanent sabres = addReady(player1, new BoneSabres());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sabres.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
                               com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
