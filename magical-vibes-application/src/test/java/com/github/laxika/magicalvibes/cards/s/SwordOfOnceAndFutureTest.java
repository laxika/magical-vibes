package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordOfOnceAndFuture.class, GrizzlyBears.class, Shock.class, Cancel.class})
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
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        var permission = gd.graveyardCardCastPermissionsUntilEndOfTurn.get(shock.getId());
        assertThat(permission).isNotNull();
        assertThat(permission.withoutPayingManaCost()).isTrue();
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
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        harness.handleMayAbilityChosen(player1, true);

        harness.castFlashback(player1, 0, target.getId());
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
}
