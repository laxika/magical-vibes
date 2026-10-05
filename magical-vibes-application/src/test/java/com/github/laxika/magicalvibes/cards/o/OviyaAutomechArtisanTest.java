package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestingBot;
import com.github.laxika.magicalvibes.cards.s.ShadowedCaravel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OviyaAutomechArtisan.class, Forest.class, GrizzlyBears.class, ShadowedCaravel.class, NestingBot.class})
class OviyaAutomechArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control gain trample while attacking an opponent")
    void attackingOwnCreatureGainsTrample() {
        addReadyOviya();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures attacking Oviya's controller do not gain trample")
    void creatureAttackingOviyaDoesNotGainTrample() {
        addReadyOviya();
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability offers creature and Vehicle cards but not other cards")
    void abilityOffersCreatureAndVehicleCards() {
        addReadyOviya();
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new ShadowedCaravel()));
        activateAbility();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.HandCardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1, 2);
    }

    @Test
    @DisplayName("Putting an artifact onto the battlefield this way adds two +1/+1 counters")
    void artifactGetsTwoPlusOnePlusOneCounters() {
        addReadyOviya();
        harness.setHand(player1, List.of(new ShadowedCaravel()));
        activateAbility();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent vehicle = findPermanent(player1, "Shadowed Caravel");
        assertThat(vehicle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Putting a nonartifact creature onto the battlefield this way adds no counters")
    void nonartifactCreatureGetsNoCounters() {
        addReadyOviya();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        activateAbility();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Grizzly Bears");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Oviya gains trample herself only while attacking an opponent")
    void oviyaGainsTrampleWhileAttacking() {
        Permanent oviya = addReadyOviya();
        assertThat(gqs.hasKeyword(gd, oviya, Keyword.TRAMPLE)).isFalse();

        oviya.setAttacking(true);
        oviya.setAttackTarget(player2.getId());
        assertThat(gqs.hasKeyword(gd, oviya, Keyword.TRAMPLE)).isTrue();

        oviya.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, oviya, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An attacking creature loses granted trample when Oviya leaves")
    void trampleEndsWhenOviyaLeaves() {
        Permanent oviya = addReadyOviya();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(oviya);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Declining the ability leaves the card in hand and still pays the tap cost")
    void mayPutCardCanBeDeclined() {
        Permanent oviya = addReadyOviya();
        harness.setHand(player1, List.of(new NestingBot()));
        activateAbility();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Nesting Bot");
        harness.assertNotOnBattlefield(player1, "Nesting Bot");
        assertThat(oviya.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting with no eligible card finishes without moving a land")
    void noEligibleCardFinishesResolution() {
        addReadyOviya();
        harness.setHand(player1, List.of(new Forest()));
        activateAbility();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An artifact creature receives counters without paying its mana cost")
    void artifactCreatureGetsCounters() {
        addReadyOviya();
        harness.setHand(player1, List.of(new NestingBot()));
        activateAbility();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Nesting Bot");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.isTapped()).isFalse();
        harness.assertNotInHand(player1, "Nesting Bot");
    }

    private void activateAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent addReadyOviya() {
        return addCreatureReady(player1, new OviyaAutomechArtisan());
    }
}
