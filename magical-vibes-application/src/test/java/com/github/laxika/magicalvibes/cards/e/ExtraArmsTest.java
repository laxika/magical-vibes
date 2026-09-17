package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.CoastWatcher;
import com.github.laxika.magicalvibes.cards.f.FierceEmpath;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtraArms.class, GoblinBrigand.class})
class ExtraArmsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the enchanted creature deals 2 damage to a target player")
    void attackingDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GoblinBrigand());
        attachExtraArms(attacker);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Attacking with the enchanted creature deals 2 damage to a target creature")
    void attackingDealsDamageToCreature() {
        Permanent attacker = addCreatureReady(player1, new GoblinBrigand());
        attachExtraArms(attacker);
        Permanent victim = addCreatureReady(player2, new GoblinBrigand());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Brigand");
        harness.assertInGraveyard(player2, "Goblin Brigand");
    }

    @Test
    @DisplayName("Attacking with the enchanted creature deals 2 damage to a target planeswalker")
    @CardUsed(ChandraNalaar.class)
    void attackingDealsDamageToPlaneswalker() {
        Permanent attacker = addCreatureReady(player1, new GoblinBrigand());
        attachExtraArms(attacker);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    @Test
    @DisplayName("The enchanted creature is the source of the triggered damage")
    @CardUsed({FierceEmpath.class, CoastWatcher.class})
    void damageUsesEnchantedCreatureAsSource() {
        Permanent attacker = addCreatureReady(player1, new FierceEmpath());
        attachExtraArms(attacker);
        Permanent protectedTarget = addCreatureReady(player2, new CoastWatcher());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, protectedTarget.getId());
        harness.passBothPriorities();

        assertThat(protectedTarget.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Coast Watcher");
    }

    private void attachExtraArms(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ExtraArms());
        aura.setAttachedTo(creature.getId());
    }
}
