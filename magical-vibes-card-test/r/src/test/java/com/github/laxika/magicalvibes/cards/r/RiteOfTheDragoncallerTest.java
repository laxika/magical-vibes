package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiteOfTheDragoncaller.class, Shock.class, Divination.class, GrizzlyBears.class})
class RiteOfTheDragoncallerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant creates a 5/5 red Dragon token with flying")
    void instantCreatesDragon() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
        Permanent dragon = findPermanent(player1, "Dragon");
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a sorcery creates a Dragon token")
    void sorceryCreatesDragon() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not create a Dragon token")
    void creatureDoesNotCreateDragon() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    @Test
    @DisplayName("The Dragon trigger resolves before the instant that caused it")
    void dragonAppearsBeforeInstantResolves() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(countPermanents(player1, "Dragon")).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's instant does not trigger Rite")
    void opponentInstantDoesNotCreateDragon() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Dragon")).isZero();
        assertThat(countPermanents(player2, "Dragon")).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Every qualifying cast creates a Dragon, including later casts in the same turn")
    void multipleCastsEachCreateDragon() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(2);
        assertThat(countPermanents(player2, "Dragon")).isZero();
    }

    @Test
    @DisplayName("Two Rites each trigger from the same spell")
    void eachRiteCreatesDragon() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(2);
        assertThat(countPermanents(player2, "Dragon")).isZero();
    }

    @Test
    @DisplayName("Casting another Rite does not create a Dragon")
    void enchantmentDoesNotCreateDragon() {
        harness.addToBattlefield(player1, new RiteOfTheDragoncaller());
        harness.setHand(player1, List.of(new RiteOfTheDragoncaller()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Rite of the Dragoncaller")).isEqualTo(2);
        assertThat(countPermanents(player1, "Dragon")).isZero();
    }
}
