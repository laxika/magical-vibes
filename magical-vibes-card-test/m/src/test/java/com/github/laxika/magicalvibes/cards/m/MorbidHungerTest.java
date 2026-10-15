package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.s.Shelter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorbidHunger.class, AvenFlock.class, Shelter.class})
class MorbidHungerTest extends BaseCardTest {

    @Test
    void dealsDamageAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new MorbidHunger()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }

    @Test
    void canTargetCreature() {
        harness.addToBattlefield(player2, new AvenFlock());
        harness.setHand(player1, List.of(new MorbidHunger()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Aven Flock");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Aven Flock");
    }

    @Test
    void flashbackDealsDamageGainsLifeAndExilesSpell() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new MorbidHunger()));
        harness.addMana(player1, ManaColor.BLACK, 9);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
        harness.assertNotInGraveyard(player1, "Morbid Hunger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Morbid Hunger"));
    }

    @Test
    void canTargetItsController() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new MorbidHunger()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Morbid Hunger");
    }

    @Test
    void gainsLifeEvenWhenCreatureSurvivesDamage() {
        harness.setLife(player1, 10);
        var creature = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new MorbidHunger()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 13);
        harness.assertOnBattlefield(player2, "Aven Flock");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void illegalTargetPreventsLifeGain() {
        harness.setLife(player1, 10);
        var creature = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setHand(player1, List.of(new MorbidHunger()));
        harness.setHand(player2, List.of(new Shelter()));
        harness.setLibrary(player2, List.of(new AvenFlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Morbid Hunger");
    }

    @Test
    void flashbackExilesSpellEvenWhenTargetBecomesIllegal() {
        harness.setLife(player1, 10);
        var creature = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        harness.setGraveyard(player1, List.of(new MorbidHunger()));
        harness.setHand(player2, List.of(new Shelter()));
        harness.setLibrary(player2, List.of(new AvenFlock()));
        harness.addMana(player1, ManaColor.BLACK, 9);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BLACK");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player1, "Morbid Hunger");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Morbid Hunger"));
    }
}
