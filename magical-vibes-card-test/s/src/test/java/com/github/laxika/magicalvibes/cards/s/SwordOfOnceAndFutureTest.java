package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BonecrusherGiant;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfOnceAndFuture.class, GrizzlyBears.class, Shock.class, Cancel.class, RampantGrowth.class, BonecrusherGiant.class})
class SwordOfOnceAndFutureTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+2 and protection from blue and black")
    void equippedCreatureGetsBoostAndProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage trigger surveils 2 and offers an instant or sorcery with mana value 2 or less")
    void combatDamageSurveilsAndOffersEligibleSpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Card topCard = new GrizzlyBears();
        Card secondCard = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setGraveyard(player1, List.of(new Cancel(), shock, new GrizzlyBears()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("A chosen eligible spell can be cast for free and is exiled instead of returning to the graveyard")
    void chosenSpellCanBeCastForFreeAndIsExiled() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Shock shock = new Shock();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock));
        resolveCombat();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("The combat damage trigger does not happen when the equipped creature is blocked")
    void blockedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Equip pays two mana and attaches the Sword to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Declining the free cast leaves the card in the graveyard")
    void canDeclineFreeCast() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The free spell may be a card just put into the graveyard by surveil")
    void canCastNewlySurveilledCard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Shock shock = new Shock();
        Card retained = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, retained));
        harness.setGraveyard(player1, List.of());

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(retained);
        harness.assertInGraveyard(player1, "Shock");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("Surveil still happens when only the opponent has an eligible graveyard card")
    void cannotCastFromOpponentsGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new Cancel()));
        harness.setGraveyard(player2, List.of(new Shock()));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An eligible sorcery is cast during combat as the trigger resolves")
    void canCastSorceryDuringCombat() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        RampantGrowth growth = new RampantGrowth();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(growth));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Rampant Growth");
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(growth);
    }

    @Test
    @DisplayName("An eligible Adventure spell is available even though its graveyard card is a creature")
    void canCastEligibleAdventureFromGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new SwordOfOnceAndFuture());
        sword.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new BonecrusherGiant()));

        resolveCombat();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }
}
