package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.m.MakeshiftMannequin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Ashnod's Intervention")
@CardUsed({AshnodsIntervention.class, ArgothianSprite.class, GoForTheThroat.class, MakeshiftMannequin.class})
class AshnodsInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +2/+0 until end of turn")
    void boostsTargetCreature() {
        Permanent creature = addCreature();

        castOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to its owner's hand when it dies")
    void returnsToHandOnDeath() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();

        castOn(creature);
        destroy(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
    }

    @Test
    @DisplayName("Creature returns to its owner's hand when it is exiled")
    void returnsToHandOnExile() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();

        castOn(creature);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The granted abilities expire at end of turn")
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();

        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        destroy(creature);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
    }

    @Test
    @DisplayName("An opposing creature returns to its owner's hand, not the spell caster's hand")
    void opposingCreatureReturnsToItsOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Card creatureCard = creature.getCard();

        castOn(creature);
        destroy(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
    }

    @Test
    @DisplayName("Returning the creature to hand does not trigger the granted ability")
    void returningToHandDoesNotTrigger() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();
        castOn(creature);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, creature));

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing exiled creature returns to its owner's hand")
    void opposingExiledCreatureReturnsToItsOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Card creatureCard = creature.getCard();
        castOn(creature);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The power boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent creature = addCreature();
        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exile after the turn ends does not return the creature")
    void exileAbilityExpiresAtEndOfTurn() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();
        castOn(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToExile(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(creatureCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
    }

    @Test
    @DisplayName("A death trigger cannot return a card that left the graveyard and died again")
    void deathTriggerDoesNotTrackCardThroughAnotherZoneChange() {
        Permanent creature = addCreature();
        Card creatureCard = creature.getCard();
        castOn(creature);
        destroy(creature);

        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, creatureCard.getId());
        Permanent returnedCreature = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(returnedCreature.getCard().getId()).isEqualTo(creatureCard.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, returnedCreature));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(creatureCard.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).doesNotContain(creatureCard.getId());
    }

    private Permanent addCreature() {
        return harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
    }

    private void castOn(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AshnodsIntervention()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }

    private void destroy(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GoForTheThroat()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
    }
}
