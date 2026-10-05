package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaddeningHex.class, Shock.class, GrizzlyBears.class, Naturalize.class})
class MaddeningHexTest extends BaseCardTest {

    @Test
    @DisplayName("Deals a random one-to-six damage when the enchanted player casts a noncreature spell")
    void triggersForEnchantedPlayersNoncreatureSpell() {
        Permanent hex = attachHexToPlayer2();
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getDescription().contains("Maddening Hex"));

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isBetween(14, 19);
        assertThat(hex.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Does not trigger when the enchanted player casts a creature spell")
    void doesNotTriggerForCreatureSpell() {
        attachHexToPlayer2();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    private Permanent attachHexToPlayer2() {
        Permanent hex = harness.addToBattlefieldAndReturn(player1, new MaddeningHex());
        hex.setAttachedTo(player2.getId());
        return hex;
    }

    @Test
    @DisplayName("Can be cast enchanting an opponent")
    void canEnchantOpponent() {
        harness.setHand(player1, List.of(new MaddeningHex()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(hex -> assertThat(hex.getAttachedTo()).isEqualTo(player2.getId()));
    }

    @Test
    @DisplayName("Does not trigger for an unenchanted player's noncreature spell")
    void doesNotTriggerForUnenchantedPlayer() {
        attachHexToPlayer2();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Enchanting its controller damages that controller and moves to their opponent")
    void canEnchantControllerAndMoveToOpponent() {
        harness.setHand(player1, List.of(new MaddeningHex(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        Permanent hex = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hex.getAttachedTo()).isEqualTo(player1.getId());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isBetween(14, 19);
        assertThat(hex.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A queued trigger still damages the spell's caster after the Aura is destroyed")
    void queuedTriggerSurvivesAuraDestruction() {
        Permanent hex = attachHexToPlayer2();
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());

        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, hex.getId());
        harness.assertNotOnBattlefield(player1, "Maddening Hex");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isBetween(14, 19);
        harness.assertInGraveyard(player1, "Maddening Hex");
    }

    @Test
    @DisplayName("Queued triggers damage the original caster even after the Aura moves")
    void queuedTriggersRememberCasterAfterReattachment() {
        Permanent hex = harness.addToBattlefieldAndReturn(player1, new MaddeningHex());
        hex.setAttachedTo(player1.getId());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        int lifeAfterFirstTrigger = gd.getLife(player1.getId());
        assertThat(lifeAfterFirstTrigger).isBetween(14, 19);
        assertThat(hex.getAttachedTo()).isEqualTo(player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId()))
                .isBetween(lifeAfterFirstTrigger - 6, lifeAfterFirstTrigger - 1);
        assertThat(hex.getAttachedTo()).isEqualTo(player2.getId());
    }
}
