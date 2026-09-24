package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GhituFire;
import com.github.laxika.magicalvibes.cards.m.MeteorStorm;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivinePresence.class, GhituFire.class, DevouringStrossus.class, MeteorStorm.class,
        ChandraNalaar.class})
class DivinePresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces four spell damage to a player with three")
    void replacesSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new DivinePresence());
        harness.setHand(player2, List.of(new GhituFire()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, 4, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Does not replace three damage")
    void leavesDamageBelowThresholdUnchanged() {
        harness.addToBattlefield(player1, new DivinePresence());
        harness.setHand(player2, List.of(new GhituFire()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, 3, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Replaces damage to a permanent")
    void replacesDamageToPermanent() {
        harness.addToBattlefield(player1, new DivinePresence());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DevouringStrossus());
        harness.setHand(player1, List.of(new GhituFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 4, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Replaces combat damage")
    void replacesCombatDamage() {
        harness.addToBattlefield(player2, new DivinePresence());
        addCreatureReady(player1, new DevouringStrossus());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Replaces four damage from an activated ability")
    void replacesActivatedAbilityDamage() {
        harness.addToBattlefield(player2, new DivinePresence());
        harness.addToBattlefield(player1, new MeteorStorm());
        harness.setHand(player1, List.of(new GhituFire(), new GhituFire()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Replaces damage to a planeswalker")
    void replacesDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new DivinePresence());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        harness.setHand(player2, List.of(new GhituFire()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.forceActivePlayer(player2);
        harness.castAndResolveSorcery(player2, 0, 4, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
    }
}
