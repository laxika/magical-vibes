package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.f.FeedTheSerpent;
import com.github.laxika.magicalvibes.cards.p.PoisonTheCup;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonicGifts.class, AxgardCavalry.class, PoisonTheCup.class})
class DemonicGiftsTest extends BaseCardTest {

    private void castOn(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void poisonTheCup(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new PoisonTheCup()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    @Test
    @DisplayName("Target creature gets +2/+0")
    void boostsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());

        castOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to the battlefield untapped when it dies")
    void returnsUntappedOnDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Card creatureCard = creature.getCard();

        castOn(creature);
        poisonTheCup(player2, creature);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature returns under its owner's control")
    void returnsUnderOwnersControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        Card creatureCard = creature.getCard();

        castOn(creature);
        poisonTheCup(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The granted death trigger wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Card creatureCard = creature.getCard();

        castOn(creature);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);

        poisonTheCup(player2, creature);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("A returned creature loses the boost and does not return from a second death")
    void returnedCreatureIsANewPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Card creatureCard = creature.getCard();

        castOn(creature);
        poisonTheCup(player2, creature);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);

        poisonTheCup(player2, returned);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Axgard Cavalry");
        harness.assertNotOnBattlefield(player1, "Axgard Cavalry");
    }

    @Test
    @DisplayName("The granted ability still triggers during the end step")
    void returnsWhenItDiesDuringEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        castOn(creature);
        harness.passUntil(TurnStep.END_STEP);

        harness.setHand(player2, List.of(new PoisonTheCup()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Axgard Cavalry");
        harness.assertNotInGraveyard(player1, "Axgard Cavalry");
    }

    @Test
    @CardUsed(FeedTheSerpent.class)
    @DisplayName("Exiling the protected creature does not trigger a return")
    void exileDoesNotTriggerReturn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        castOn(creature);

        harness.setHand(player2, List.of(new FeedTheSerpent()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(creature.getCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Axgard Cavalry");
    }

    @Test
    @DisplayName("Demonic Gifts does not protect a creature killed in response")
    void creatureDiesBeforeGiftsResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DemonicGifts()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());

        poisonTheCup(player2, creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Axgard Cavalry");
        harness.assertNotOnBattlefield(player1, "Axgard Cavalry");
    }

    @Test
    @DisplayName("A remaining return trigger cannot return the card after it dies again")
    void olderTriggerCannotReturnCardFromAnotherDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Card creatureCard = creature.getCard();
        castOn(creature);
        castOn(creature);

        poisonTheCup(player2, creature);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst().orElseThrow();
        poisonTheCup(player2, returned);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Axgard Cavalry");
        harness.assertNotOnBattlefield(player1, "Axgard Cavalry");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A stolen creature returns to its owner rather than its controller")
    void stolenCreatureReturnsToOwner() {
        AxgardCavalry card = new AxgardCavalry();
        card.setOwnerId(player1.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        castOn(creature);
        poisonTheCup(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(card.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(card.getId()));
        harness.assertNotInGraveyard(player1, "Axgard Cavalry");
    }
}
