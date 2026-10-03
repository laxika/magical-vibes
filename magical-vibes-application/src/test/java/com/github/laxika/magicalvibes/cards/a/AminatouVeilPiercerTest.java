package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DefenseGrid;
import com.github.laxika.magicalvibes.cards.f.FormOfTheDragon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheMasterOfKeys;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AminatouVeilPiercer.class, FormOfTheDragon.class, GrizzlyBears.class, TheMasterOfKeys.class, DefenseGrid.class})
class AminatouVeilPiercerTest extends BaseCardTest {

    @Test
    @DisplayName("Surveils two at the beginning of its controller's upkeep")
    void surveilsTwoAtUpkeep() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Grants enchantment cards miracle for their mana cost reduced by four")
    void grantsReducedMiracleToEnchantmentCards() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        FormOfTheDragon form = new FormOfTheDragon();
        harness.setLibrary(player1, List.of(form));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        PendingInteraction.MayAbilityChoice choice =
                (PendingInteraction.MayAbilityChoice) gd.interaction.activeInteraction();
        assertThat(choice.manaCost()).isEqualTo("{R}{R}{R}");

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Form of the Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Does not grant miracle to non-enchantment cards")
    void doesNotGrantMiracleToNonEnchantmentCards() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotGrantMiracleWhileFaceDown() {
        var aminatou = harness.addToBattlefieldAndReturn(player1, new AminatouVeilPiercer());
        aminatou.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setLibrary(player1, List.of(new TheMasterOfKeys()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotGrantMiracleAfterLosingAbilities() {
        var aminatou = harness.addToBattlefieldAndReturn(player1, new AminatouVeilPiercer());
        aminatou.setLosesAllAbilitiesUntilEndOfTurn(true);
        harness.setLibrary(player1, List.of(new TheMasterOfKeys()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void miracleReductionAppliesToChosenX() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        harness.setLibrary(player1, List.of(new TheMasterOfKeys()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AlternateCastXValueChoice.class);
        harness.handleXValueChosen(player1, 4);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(4);
        harness.assertNotInHand(player1, "The Master of Keys");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "The Master of Keys");
        harness.passBothPriorities();
    }

    @Test
    void doesNotGrantMiracleToOpponentsHand() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        harness.setLibrary(player2, List.of(new TheMasterOfKeys()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void secondCardDrawnDoesNotHaveAMiracleOpportunity() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        harness.setLibrary(player1, List.of(new AminatouVeilPiercer(), new TheMasterOfKeys()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void surveilCanKeepCardsInEitherOrder() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        Card first = new AminatouVeilPiercer();
        Card second = new TheMasterOfKeys();
        harness.setLibrary(player1, List.of(first, second));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void miracleStillRequiresPayingCostIncreasesOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        harness.addToBattlefield(player2, new DefenseGrid());
        FormOfTheDragon form = new FormOfTheDragon();
        harness.setLibrary(player1, List.of(form));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(form);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void canDeclineToRevealAnEnchantment() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        TheMasterOfKeys card = new TheMasterOfKeys();
        harness.setLibrary(player1, List.of(card));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDeclineToCastAfterRevealingAnEnchantment() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        TheMasterOfKeys card = new TheMasterOfKeys();
        harness.setLibrary(player1, List.of(card));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getPlayerInputService().processNextMayAbility(gd));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotSurveilDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AminatouVeilPiercer());
        Card card = new TheMasterOfKeys();
        harness.setLibrary(player1, List.of(card));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
