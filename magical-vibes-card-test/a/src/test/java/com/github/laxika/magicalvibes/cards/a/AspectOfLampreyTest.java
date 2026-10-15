package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AspectOfLamprey.class, NyxbornCourser.class})
class AspectOfLampreyTest extends BaseCardTest {

    @Test
    @DisplayName("When Aspect of Lamprey enters, target opponent discards two cards")
    void etbMakesTargetOpponentDiscardTwoCards() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player2, List.of(
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser()));
        harness.setHand(player1, List.of(new AspectOfLamprey()));
        addCastingMana();

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Enchanted creature has lifelink while Aspect of Lamprey remains attached")
    void enchantedCreatureHasLifelink() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AspectOfLamprey());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Aspect of Lamprey can enchant only a creature its controller controls")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        harness.addToBattlefield(player1, new NyxbornCourser());
        harness.setHand(player1, List.of(new AspectOfLamprey()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Aspect of Lamprey's enters trigger must target an opponent")
    void entersTriggerMustTargetOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player1, List.of(new AspectOfLamprey()));
        addCastingMana();

        harness.setHand(player2, List.of());
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
    }

    @Test
    void opponentWithOneCardDiscardsItAndResolutionCompletes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player2, List.of(new NyxbornCourser()));
        harness.setHand(player1, List.of(new AspectOfLamprey()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void emptyHandDoesNotPreventAuraFromResolving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AspectOfLamprey()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void missingAuraTargetPreventsEntryAndDiscardTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player2, List.of(new NyxbornCourser(), new NyxbornCourser()));
        harness.setHand(player1, List.of(new AspectOfLamprey()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AspectOfLamprey);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void discardTriggerResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player2, List.of(new NyxbornCourser(), new NyxbornCourser()));
        harness.setHand(player1, List.of(new AspectOfLamprey()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Aspect of Lamprey"));
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void enchantedCreatureCombatDamageGainsLife() {
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AspectOfLamprey());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void auraFallsOffWhenCreatureIsNoLongerControlledByAuraController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AspectOfLamprey());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AspectOfLamprey);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
