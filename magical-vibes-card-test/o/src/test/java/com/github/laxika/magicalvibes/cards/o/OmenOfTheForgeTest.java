package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.e.ElspethSunsNemesis;
import com.github.laxika.magicalvibes.cards.n.NyxbornBrute;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmenOfTheForge.class, NyxbornBrute.class, ElspethSunsNemesis.class})
class OmenOfTheForgeTest extends BaseCardTest {

    @Test
    void enteringBattlefieldDealsTwoDamageToTargetPlayer() {
        harness.setHand(player1, List.of(new OmenOfTheForge()));
        addCastingMana();

        harness.castEnchantment(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Omen of the Forge");
    }

    @Test
    void enteringBattlefieldDealsTwoDamageToTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NyxbornBrute());
        harness.setHand(player1, List.of(new OmenOfTheForge()));
        addCastingMana();

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Omen of the Forge");
    }

    @Test
    void sacrificesToScryTwo() {
        Card firstCard = new NyxbornBrute();
        Card secondCard = new NyxbornBrute();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheForge());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(omen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(omen.getCard());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(firstCard, secondCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard, firstCard);
    }

    @Test
    void canBeCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new OmenOfTheForge()));
        addCastingMana();

        harness.castEnchantment(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Omen of the Forge");
    }

    @Test
    void enteringBattlefieldDealsDamageToPlaneswalker() {
        Permanent elspeth = harness.addToBattlefieldAndReturn(player2, new ElspethSunsNemesis());
        elspeth.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new OmenOfTheForge()));
        addCastingMana();

        harness.castEnchantment(player1, 0, elspeth.getId());
        resolveAllTriggers();

        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void damageTriggerStillResolvesAfterSacrificingOmen() {
        harness.setHand(player1, List.of(new OmenOfTheForge()));
        harness.setLibrary(player1, List.of());
        addCastingMana();
        addAbilityMana();
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertInGraveyard(player1, "Omen of the Forge");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scryCanReorderTopCardsWithoutMovingTheRestOfLibrary() {
        Card first = new NyxbornBrute();
        Card second = new OmenOfTheForge();
        Card third = new NyxbornBrute();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addToBattlefield(player1, new OmenOfTheForge());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
    }

    @Test
    void scryCanPutBothCardsOnBottomInChosenOrder() {
        Card first = new NyxbornBrute();
        Card second = new OmenOfTheForge();
        Card third = new NyxbornBrute();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addToBattlefield(player1, new OmenOfTheForge());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
    }

    @Test
    void scryWithOneCardLooksAtOnlyThatCard() {
        Card onlyCard = new NyxbornBrute();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addToBattlefield(player1, new OmenOfTheForge());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
