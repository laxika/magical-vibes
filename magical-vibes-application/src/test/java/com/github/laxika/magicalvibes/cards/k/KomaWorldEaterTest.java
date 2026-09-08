package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KomaWorldEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        KomaWorldEater koma = new KomaWorldEater();
        harness.setHand(player1, List.of(koma));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, koma.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Koma, World-Eater");
        harness.assertNotInGraveyard(player1, "Koma, World-Eater");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Creates four 3/3 blue Serpent tokens when dealing combat damage to a player")
    void createsKomasCoilsOnCombatDamage() {
        Permanent koma = addCreatureReady(player1, new KomaWorldEater());
        koma.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Koma's Coil")).hasSize(4)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(3);
                    assertThat(token.getCard().getToughness()).isEqualTo(3);
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
                });
    }

    @Test
    @DisplayName("Does not create tokens without combat damage to a player")
    void doesNotCreateKomasCoilsWithoutCombatDamageToPlayer() {
        Permanent koma = addCreatureReady(player1, new KomaWorldEater());
        koma.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, creature("Wall", 0, 8));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Koma's Coil")).isEmpty();
    }

    private static com.github.laxika.magicalvibes.model.Card creature(String name, int power, int toughness) {
        com.github.laxika.magicalvibes.model.Card card = new com.github.laxika.magicalvibes.model.Card();
        card.setName(name);
        card.setType(com.github.laxika.magicalvibes.model.CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
