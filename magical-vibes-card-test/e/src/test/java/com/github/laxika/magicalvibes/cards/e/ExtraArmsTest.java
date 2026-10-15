package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.CoastWatcher;
import com.github.laxika.magicalvibes.cards.f.FierceEmpath;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtraArms.class, GoblinBrigand.class, ChandraNalaar.class, FierceEmpath.class, CoastWatcher.class})
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

    @Test
    @DisplayName("The Aura controller chooses the target when an opponent's enchanted creature attacks")
    void auraControllerChoosesTargetForOpponentsAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player2, new GoblinBrigand());
        attachExtraArms(attacker);

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The attack trigger can target and kill the enchanted creature before combat damage")
    void attackTriggerCanKillItsDamageSource() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GoblinBrigand());
        attachExtraArms(attacker);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Goblin Brigand");
        harness.assertInGraveyard(player1, "Extra Arms");
        harness.assertNotOnBattlefield(player1, "Goblin Brigand");
        harness.assertNotOnBattlefield(player1, "Extra Arms");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting Extra Arms attaches it to the targeted creature and enables its attack trigger")
    void castAuraEnablesAttackTrigger() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GoblinBrigand());
        harness.setHand(player1, List.of(new ExtraArms()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Extra Arms").getAttachedTo()).isEqualTo(attacker.getId());
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    private void attachExtraArms(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ExtraArms());
        aura.setAttachedTo(creature.getId());
    }
}
