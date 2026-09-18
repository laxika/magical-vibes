package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinGoliath.class, Shock.class, SerraAngel.class, GrizzlyBears.class})
class GoblinGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates one Goblin token for the opponent")
    void createsGoblinForOpponent() {
        harness.setHand(player1, List.of(new GoblinGoliath()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> goblins = findPermanents(player1, "Goblin");
        assertThat(goblins).hasSize(1);
        assertThat(goblins.getFirst().getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(goblins.getFirst().getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblins.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(goblins.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles damage from your sources to an opponent")
    void doublesDamageToOpponent() {
        harness.addToBattlefield(player1, new GoblinGoliath());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not double damage to an opponent's permanent")
    void doesNotDoubleDamageToOpponentPermanent() {
        harness.addToBattlefield(player1, new GoblinGoliath());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Does not double damage from an opponent's source")
    void doesNotDoubleOpponentsSourceDamage() {
        harness.addToBattlefield(player1, new GoblinGoliath());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles combat damage to an opponent")
    void doublesCombatDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new GoblinGoliath());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }
}
