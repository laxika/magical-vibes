package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BitterFeud.class, Shock.class, SerraAngel.class})
class BitterFeudTest extends BaseCardTest {

    @Test
    @DisplayName("As it enters, Bitter Feud chooses two distinct players")
    void choosesTwoPlayersOnEntry() {
        Permanent feud = castBitterFeudSpell();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(feud.getChosenPlayerIds()).containsExactly(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Doubles damage from either chosen player to the other chosen player")
    void doublesDamageBetweenChosenPlayers() {
        castBitterFeud();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Doubles damage to a permanent controlled by the chosen player")
    void doublesDamageToChosenPlayersPermanent() {
        castBitterFeud();
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, angel.getId());

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Doubles damage from the second chosen player to the first")
    void doublesDamageInReverseDirection() {
        castBitterFeud();
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Does not double damage a chosen player deals to themselves")
    void doesNotDoubleSelfDamage() {
        castBitterFeud();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does not double damage to the source controller's own permanent")
    void doesNotDoubleDamageToOwnPermanent() {
        castBitterFeud();
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(angel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Bitter Feuds each double the damage")
    void multipleFeudsQuadrupleDamage() {
        castBitterFeud();
        castBitterFeud();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("Doubles combat damage to the other chosen player")
    void doublesCombatDamage() {
        castBitterFeud();
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SerraAngel());

        declareAttackers(player1, List.of(1));
        resolveCombat();

        harness.assertLife(player2, 12);
    }

    private Permanent castBitterFeud() {
        Permanent feud = castBitterFeudSpell();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        return feud;
    }

    private Permanent castBitterFeudSpell() {
        harness.setHand(player1, List.of(new BitterFeud()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent feud = findPermanent(player1, "Bitter Feud");
        return feud;
    }
}
