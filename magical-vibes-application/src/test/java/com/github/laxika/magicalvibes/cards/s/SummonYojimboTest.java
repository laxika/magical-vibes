package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SummonYojimbo.class, FountainOfYouth.class, GrizzlyBears.class})
class SummonYojimboTest extends BaseCardTest {

    @Test
    void chapterIExilesAnOpponentArtifactOrEnchantmentOrTappedCreature() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedCreature.tap();

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId(), tappedCreature.getId())
                .doesNotContain(ownCreature.getId(), untappedCreature.getId());

        harness.handlePermanentChosen(player1, tappedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(tappedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    void chapterIIChargesTwoManaForEachCreatureAttackingControllerUntilNextTurn() {
        addSagaWithLore(1);
        advanceToNextChapter();
        harness.passBothPriorities();

        addReadyCreature(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        declareAttackers(player2, List.of(0), null);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void chapterIITaxDoesNotApplyToAttacksAgainstPlaneswalkers() {
        addSagaWithLore(1);
        Permanent planeswalker = addPlaneswalker(player1, 4);
        advanceToNextChapter();
        harness.passBothPriorities();

        addReadyCreature(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    void chapterIIIRepeatsTheAttackTax() {
        addSagaWithLore(2);
        advanceToNextChapter();
        harness.passBothPriorities();

        addReadyCreature(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    @Test
    void chapterIVCreatesOneTreasureForAnOpponentWithA4PowerCreature() {
        addSagaWithLore(3);
        harness.addToBattlefield(player2, testCreature("Test 4/4", 4, 4));

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Treasure");
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonYojimbo());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, java.util.UUID> targets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, targets);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);

        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }

    private Card testCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
