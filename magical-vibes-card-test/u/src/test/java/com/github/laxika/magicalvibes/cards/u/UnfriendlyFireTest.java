package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnfriendlyFire.class, QueensBaySoldier.class, ColossalDreadmaw.class, JaceCunningCastaway.class})
class UnfriendlyFireTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Unfriendly Fire targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Unfriendly Fire");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Unfriendly Fire targeting a creature puts it on the stack")
    void castingTargetingCreaturePutsItOnStack() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Queen's Bay Soldier");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Unfriendly Fire");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Cannot cast Unfriendly Fire without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Unfriendly Fire deals 4 damage to target player")
    void deals4DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Unfriendly Fire deals 4 damage to target creature, destroying a 2/2")
    void deals4DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new QueensBaySoldier());
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Queen's Bay Soldier");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Queen's Bay Soldier");
        harness.assertInGraveyard(player2, "Queen's Bay Soldier");
    }

    @Test
    @DisplayName("Unfriendly Fire goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Unfriendly Fire");
    }

    @Test
    @DisplayName("Unfriendly Fire marks four damage on a surviving creature")
    void marksFourDamageOnSurvivingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Colossal Dreadmaw");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unfriendly Fire can deal lethal damage to a planeswalker")
    void dealsLethalDamageToPlaneswalker() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new JaceCunningCastaway());
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Jace, Cunning Castaway");
        harness.assertInGraveyard(player2, "Jace, Cunning Castaway");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unfriendly Fire can target its controller")
    void canTargetController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new UnfriendlyFire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }
}
