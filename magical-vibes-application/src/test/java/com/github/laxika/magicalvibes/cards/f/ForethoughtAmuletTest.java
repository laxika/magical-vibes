package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForethoughtAmulet.class, Blaze.class, GrizzlyBears.class, LavaSpike.class, SerraAngel.class,
        Shock.class})
class ForethoughtAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces three damage from a sorcery to its controller with two")
    void replacesSorceryDamageToController() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        harness.setHand(player2, List.of(new LavaSpike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not replace damage below three")
    void leavesSmallerDamageUnchanged() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not replace instant or sorcery damage to a permanent")
    void doesNotReplaceDamageToPermanent() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 3, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not replace combat damage")
    void doesNotReplaceCombatDamage() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        GrizzlyBears attacker = new GrizzlyBears();
        attacker.setPower(3);
        attacker.setToughness(3);
        addCreatureReady(player2, attacker);

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Paying three mana during upkeep keeps Forethought Amulet")
    void payingUpkeepKeepsAmulet() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new ForethoughtAmulet());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(amulet);
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Forethought Amulet")
    void decliningUpkeepSacrificesAmulet() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new ForethoughtAmulet());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(amulet);
        harness.assertInGraveyard(player1, "Forethought Amulet");
    }
}
