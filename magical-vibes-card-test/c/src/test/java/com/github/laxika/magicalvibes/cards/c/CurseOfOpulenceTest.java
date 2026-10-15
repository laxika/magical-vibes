package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfOpulence.class, GrizzlyBears.class, JaceBeleren.class})
class CurseOfOpulenceTest extends BaseCardTest {

    @Test
    @DisplayName("The Curse controller and an attacking opponent each create one Gold")
    void controllerAndAttackingOpponentCreateGold() {
        placeCurseOnPlayer(player1, player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).hasSize(1);
    }

    @Test
    @DisplayName("The Curse controller does not get a second Gold for attacking the enchanted player")
    void controllerDoesNotCreateTheOpponentGold() {
        placeCurseOnPlayer(player1, player2);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).isEmpty();
    }

    @Test
    @DisplayName("Attacking an enchanted player's planeswalker does not trigger the Curse")
    void attackingPlaneswalkerDoesNotTrigger() {
        placeCurseOnPlayer(player1, player1);
        addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).isEmpty();
        assertThat(findPermanents(player2, "Gold")).isEmpty();
    }

    private void placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent curse = harness.addToBattlefieldAndReturn(controller, new CurseOfOpulence());
        curse.setAttachedTo(enchantedPlayer.getId());
    }

    @Test
    @DisplayName("The Aura can be cast enchanting its controller")
    void canEnchantItsController() {
        harness.setHand(player1, List.of(new CurseOfOpulence()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Curse of Opulence").getAttachedTo()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The Aura can be cast enchanting an opponent")
    void canEnchantOpponent() {
        harness.setHand(player1, List.of(new CurseOfOpulence()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Curse of Opulence").getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Attacking both the enchanted player and their planeswalker triggers once")
    void mixedAttackTargetsCreateOneGoldEach() {
        placeCurseOnPlayer(player1, player1);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0, 1),
                Map.of(0, player1.getId(), 1, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).hasSize(1);
    }

    @Test
    @DisplayName("A queued trigger creates Gold even if the Curse leaves the battlefield")
    void triggerSurvivesRemovalOfCurse() {
        placeCurseOnPlayer(player1, player1);
        Permanent curse = findPermanent(player1, "Curse of Opulence");
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(curse);
        gd.playerGraveyards.get(player1.getId()).add(curse.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).hasSize(1);
    }

    @Test
    void triggerUsesThePlayerEnchantedWhenTheAttackWasDeclared() {
        placeCurseOnPlayer(player1, player1);
        Permanent curse = findPermanent(player1, "Curse of Opulence");
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        curse.setAttachedTo(player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).hasSize(1);
    }

    @Test
    @DisplayName("Only the Curse controller creates Gold when all attackers leave before resolution")
    void attackingOpponentGetsNoGoldAfterAllAttackersLeave() {
        placeCurseOnPlayer(player1, player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(attacker);
        gd.playerGraveyards.get(player2.getId()).add(attacker.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        assertThat(findPermanents(player2, "Gold")).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Tapped Gold can be sacrificed for one mana of any color without using the stack")
    void tappedGoldProducesChosenColor(ManaColor color) {
        placeCurseOnPlayer(player1, player2);
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        Permanent gold = findPermanent(player1, "Gold");
        gold.tap();
        int goldIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gold);

        harness.activateAbility(player1, goldIndex, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Gold")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
