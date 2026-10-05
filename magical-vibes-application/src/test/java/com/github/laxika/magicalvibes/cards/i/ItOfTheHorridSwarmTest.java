package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GeistlightSnare;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ItOfTheHorridSwarm.class, GrizzlyBears.class, GeistlightSnare.class})
class ItOfTheHorridSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast: when cast, create two 1/1 green Insect tokens")
    void hardcastCreatesInsects() {
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "It of the Horrid Swarm");
        List<Permanent> insects = findPermanents(player1, "Insect");
        assertThat(insects).hasSize(2);
        assertThat(insects).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.INSECT);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Emerge: sacrifice a creature, pay emerge cost reduced by its mana value")
    void emergeSacrificesAndReducesCost() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        // Emerge {6}{G} reduced by 2 → {4}{G}
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("It of the Horrid Swarm"))
                .noneMatch(p -> p.getId().equals(bearsId));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        assertThat(findPermanents(player1, "Insect")).hasSize(2)
                .allSatisfy(insect -> assertThat(insect.getCard().isToken()).isTrue());
    }

    @Test
    @DisplayName("Emerge fails without enough mana after reduction")
    void emergeFailsWithInsufficientMana() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        // Need {4}{G} after reduction; only {3}{G} available
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.castCreatureWithAlternateCost(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cast trigger resolves before the creature spell")
    void castTriggerResolvesBeforeCreature() {
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities(); // resolve trigger only

        assertThat(findPermanents(player1, "Insect")).hasSize(2)
                .allSatisfy(insect -> assertThat(insect.getCard().isToken()).isTrue());
        harness.assertNotOnBattlefield(player1, "It of the Horrid Swarm");
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("It of the Horrid Swarm");
    }

    @Test
    @DisplayName("Sacrificing a mana value eight creature reduces emerge to one green mana")
    void emergeReductionCannotReduceColoredMana() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new ItOfTheHorridSwarm()).getId();
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "It of the Horrid Swarm")).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
        harness.assertInGraveyard(player1, "It of the Horrid Swarm");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Even a large emerge reduction still requires green mana")
    void emergeFailsWithoutGreenMana() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player1, new ItOfTheHorridSwarm()).getId();
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(findPermanent(player1, "It of the Horrid Swarm").getId()).isEqualTo(sacrificeId);
        harness.assertNotInGraveyard(player1, "It of the Horrid Swarm");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the emerge sacrifice cost")
    void emergeCannotSacrificeOpponentsCreature() {
        UUID sacrificeId = harness.addToBattlefieldAndReturn(player2, new ItOfTheHorridSwarm()).getId();
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificeId)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "It of the Horrid Swarm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Insect token can be sacrificed for emerge but gives no mana reduction")
    void emergeCanSacrificeZeroManaValueToken() {
        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        UUID insectId = findPermanent(player1, "Insect").getId();

        harness.setHand(player1, List.of(new ItOfTheHorridSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of(insectId));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Insect")).hasSize(3)
                .noneMatch(insect -> insect.getId().equals(insectId));
        assertThat(countPermanents(player1, "It of the Horrid Swarm")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Entering the battlefield without being cast creates no Insects")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.enterBattlefieldAndReturn(player1, new ItOfTheHorridSwarm());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "It of the Horrid Swarm");
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("The cast trigger still creates Insects when the creature spell is countered")
    void counteringCreatureDoesNotCounterCastTrigger() {
        ItOfTheHorridSwarm swarm = new ItOfTheHorridSwarm();
        harness.setHand(player1, List.of(swarm));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.setHand(player2, List.of(new GeistlightSnare()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, swarm.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "It of the Horrid Swarm");
        harness.assertNotOnBattlefield(player1, "It of the Horrid Swarm");
        assertThat(countPermanents(player1, "Insect")).isZero();
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
        assertThat(countPermanents(player2, "Insect")).isZero();
    }
}
