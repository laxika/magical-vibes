package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.cards.s.StormSeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForethoughtAmulet.class, DurkwoodBoars.class, Pyrotechnics.class, StormSeeker.class})
class ForethoughtAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces three damage from a sorcery to its controller with two")
    void replacesSorceryDamageToController() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        harness.setHand(player2, List.of(new Pyrotechnics()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, Map.of(player1.getId(), 3, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not replace damage below three")
    void leavesSmallerDamageUnchanged() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        harness.setHand(player1, List.of(new ForethoughtAmulet(), new ForethoughtAmulet()));
        harness.setHand(player2, List.of(new StormSeeker()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not replace instant or sorcery damage to a permanent")
    void doesNotReplaceDamageToPermanent() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        Permanent boar = harness.addToBattlefieldAndReturn(player1, new DurkwoodBoars());
        harness.setHand(player2, List.of(new Pyrotechnics()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, Map.of(boar.getId(), 3, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(boar.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not replace combat damage")
    void doesNotReplaceCombatDamage() {
        harness.addToBattlefield(player1, new ForethoughtAmulet());
        addCreatureReady(player2, new DurkwoodBoars());

        declareAttackers(player2, List.of(0));

        harness.assertLife(player1, 16);
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

    @Test
    @DisplayName("The upkeep payment is not requested during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent amulet = harness.addToBattlefieldAndReturn(player1, new ForethoughtAmulet());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(amulet);
    }
}
