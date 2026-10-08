package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YoungPyromancer.class, Shock.class, Divination.class, GrizzlyBears.class})
class YoungPyromancerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a 1/1 Elemental token")
    void instantCreatesElemental() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery creates an Elemental token")
    void sorceryCreatesElemental() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not create an Elemental token")
    void creatureSpellCreatesNoElemental() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("The token trigger resolves before the spell")
    void tokenResolvesBeforeSpell() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Elemental")).isZero();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's spell does not trigger Young Pyromancer")
    void opponentSpellCreatesNoElemental() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(countPermanents(player2, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Each Young Pyromancer triggers for each qualifying spell")
    void multiplePyromancersAndSpellsCreateSeparateTokens() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(4);
        assertThat(countPermanents(player2, "Elemental")).isZero();
    }

    @Test
    @DisplayName("A pending trigger still creates a token after Young Pyromancer dies")
    void pendingTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new YoungPyromancer());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Young Pyromancer"));
        harness.assertNotOnBattlefield(player1, "Young Pyromancer");
        assertThat(countPermanents(player1, "Elemental")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        harness.passBothPriorities();
    }
}
