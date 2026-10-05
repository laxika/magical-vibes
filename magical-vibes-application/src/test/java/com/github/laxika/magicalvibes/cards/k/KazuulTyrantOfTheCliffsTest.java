package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KazuulTyrantOfTheCliffs.class, ArborElf.class, InvasionOfZendikar.class,
        JaceTheMindSculptor.class})
class KazuulTyrantOfTheCliffsTest extends BaseCardTest {

    @Test
    @DisplayName("Declining to pay creates a 3/3 red Ogre token")
    void decliningToPayCreatesOgreToken() {
        addKazuul(player1);
        addCreatureReady(player2, new ArborElf());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).singleElement().satisfies(ogre -> {
            assertThat(ogre.getCard().isToken()).isTrue();
            assertThat(ogre.getCard().getPower()).isEqualTo(3);
            assertThat(ogre.getCard().getToughness()).isEqualTo(3);
            assertThat(ogre.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(ogre.getCard().getSubtypes()).contains(CardSubtype.OGRE);
        });
    }

    @Test
    @DisplayName("The attacking creature's controller may pay to prevent the token")
    void attackerControllerMayPay() {
        addKazuul(player1);
        addCreatureReady(player2, new ArborElf());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Ogre")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Each attacking creature causes a separate token-or-payment trigger")
    void triggersForEachAttackingCreature() {
        addKazuul(player1);
        addCreatureReady(player2, new ArborElf());
        addCreatureReady(player2, new ArborElf());

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).hasSize(2);
    }

    @Test
    void attackerLeavingBeforeResolutionStillOffersItsLastControllerPayment() {
        addKazuul(player1);
        var attacker = addCreatureReady(player2, new ArborElf());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerGraveyards.get(player2.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Ogre")).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void triggerCreatesTokenEvenAfterKazuulLeavesTheBattlefield() {
        var kazuul = addCreatureReady(player1, new KazuulTyrantOfTheCliffs());
        addCreatureReady(player2, new ArborElf());

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(kazuul);
        gd.playerGraveyards.get(player1.getId()).add(kazuul.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).hasSize(1);
    }

    @Test
    void paymentIsOptionalIndependentlyForEachAttacker() {
        addKazuul(player1);
        addCreatureReady(player2, new ArborElf());
        addCreatureReady(player2, new ArborElf());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void attackingAPlaneswalkerAlsoCreatesAToken() {
        addKazuul(player1);
        addCreatureReady(player2, new ArborElf());
        var planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceTheMindSculptor());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).hasSize(1);
    }

    @Test
    void attackingABattleTriggersForItsProtector() {
        addKazuul(player1);
        addCreatureReady(player2, new ArborElf());
        var battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, battle.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Ogre")).hasSize(1);
    }

    private void addKazuul(Player player) {
        addCreatureReady(player, new KazuulTyrantOfTheCliffs());
    }
}
