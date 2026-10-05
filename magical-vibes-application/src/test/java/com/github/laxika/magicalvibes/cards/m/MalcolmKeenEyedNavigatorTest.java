package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.r.ReefPirates;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalcolmKeenEyedNavigator.class, ReefPirates.class, GrizzlyBears.class, HermeticStudy.class,
        ArtificialEvolution.class, Tarfire.class})
class MalcolmKeenEyedNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when a Pirate deals combat damage to an opponent")
    void createsTreasureWhenPirateDealsCombatDamage() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        declareAttackers(List.of(0));

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates only one Treasure when multiple Pirates deal damage to one opponent")
    void batchesMultiplePiratesDealingDamage() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        addCreatureReady(player1, new ReefPirates());
        declareAttackers(List.of(0, 1));

        resolveCombat();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Treasure when a Pirate deals noncombat damage to an opponent")
    void createsTreasureForNoncombatDamage() {
        Permanent malcolm = addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(malcolm.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Treasure when a non-Pirate deals damage")
    void doesNotTriggerForNonPirate() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Does not create a Treasure when a Pirate deals damage to its controller")
    void doesNotTriggerForDamageToController() {
        Permanent malcolm = addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(malcolm.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Creates a Treasure when a noncreature Pirate spell damages an opponent")
    void createsTreasureForPirateKindredSpell() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        harness.setHand(player1, List.of(new Tarfire(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        UUID tarfireId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player1, 0, tarfireId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player1, "PIRATE");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a Pirate controlled by an opponent")
    void doesNotTriggerForOpponentsPirate() {
        addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent pirate = addCreatureReady(player2, new ReefPirates());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HermeticStudy());
        aura.setAttachedTo(pirate.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    @DisplayName("Does not trigger when a Pirate damages an opponent's creature")
    void doesNotTriggerForDamageToCreature() {
        Permanent malcolm = addCreatureReady(player1, new MalcolmKeenEyedNavigator());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(malcolm.getId());

        harness.activateAbility(player1, 0, null, bears.getId());
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
