package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.d.Duress;
import com.github.laxika.magicalvibes.cards.e.ExquisiteBlood;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.ObstinateBaloth;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfessorOnyx.class, BarkshellBlessing.class, GiantGrowth.class, GrizzlyBears.class,
        HillGiant.class, SerraAngel.class, Duress.class, ObstinateBaloth.class, ExquisiteBlood.class})
class ProfessorOnyxTest extends BaseCardTest {

    @Test
    @DisplayName("Magecraft drains each opponent when you cast an instant")
    void magecraftTriggersOnCast() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Magecraft triggers for each copied instant")
    void magecraftTriggersOnCopy() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("+1 loses life and puts one of the top three cards into hand")
    void plusOneLooksAtTopThree() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        Card chosen = new GiantGrowth();
        Card graveyardCard = new GrizzlyBears();
        Card otherGraveyardCard = new BarkshellBlessing();
        harness.setLibrary(player1, List.of(chosen, graveyardCard, otherGraveyardCard));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(graveyardCard, otherGraveyardCard);
    }

    @Test
    @DisplayName("-3 sacrifices each opponent's greatest-power creature")
    void minusThreeSacrificesGreatestPowerCreature() {
        Permanent onyx = addReadyOnyx(player1, 5);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(onyx.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("-8 repeats the discard-or-life-loss process seven times")
    void minusEightRepeatsSevenTimes() {
        addReadyOnyx(player1, 8);
        harness.setLife(player2, 30);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(9);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-8 lets an opponent discard instead of losing life")
    void minusEightAllowsDiscarding() {
        addReadyOnyx(player1, 8);
        harness.setLife(player2, 20);
        Card discarded = new GiantGrowth();
        harness.setHand(player2, List.of(discarded));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.getLife(player2.getId())).isEqualTo(2);
        harness.assertInGraveyard(player2, discarded.getName());
    }

    @Test
    void magecraftTriggersOnSorcery() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Duress()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void magecraftDoesNotTriggerForCreatureOrOpponentsInstant() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent target = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void plusOneRequiresPuttingOneCardIntoHand() {
        addReadyOnyx(player1, 5);
        Card chosen = new GiantGrowth();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, other));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    void plusOneWithOneCardPutsItIntoHandWithoutDrawing() {
        Permanent onyx = addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        Card card = new GiantGrowth();
        harness.setLibrary(player1, List.of(card));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(onyx.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void plusOneStillLosesLifeWithEmptyLibrary() {
        addReadyOnyx(player1, 5);
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusThreeLetsOpponentChooseAmongTiedGreatestPowerCreatures() {
        addReadyOnyx(player1, 5);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent smaller = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, smaller).doesNotContain(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(second.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(own);
    }

    @Test
    void minusThreeUsesCurrentPower() {
        addReadyOnyx(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(angel).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears.getCard());
    }

    @Test
    void minusEightAllowsDecliningEveryDiscardWithCardsInHand() {
        addReadyOnyx(player1, 8);
        harness.setLife(player2, 30);
        Card retained = new GiantGrowth();
        harness.setHand(player2, List.of(retained));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        for (int i = 0; i < 7; i++) {
            harness.handleMayAbilityChosen(player2, false);
        }

        harness.assertLife(player2, 9);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusThreeDoesNothingWhenOpponentControlsNoCreatures() {
        addReadyOnyx(player1, 5);
        harness.addToBattlefield(player2, new ExquisiteBlood());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Exquisite Blood");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void minusEightCanDiscardSevenCardsWithoutLosingLife() {
        addReadyOnyx(player1, 8);
        harness.setLife(player2, 30);
        List<Card> cards = List.of(new GiantGrowth(), new GiantGrowth(), new GiantGrowth(),
                new GiantGrowth(), new GiantGrowth(), new GiantGrowth(), new GiantGrowth());
        harness.setHand(player2, cards);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        for (int i = 0; i < 7; i++) {
            harness.handleMayAbilityChosen(player2, true);
            harness.handleCardChosen(player2, 0);
        }

        harness.assertLife(player2, 30);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsAll(cards);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningUltimateDiscardTriggersLifeLossAbilities() {
        addReadyOnyx(player1, 8);
        harness.addToBattlefield(player1, new ExquisiteBlood());
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);
        harness.setHand(player2, List.of(new GiantGrowth()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        for (int i = 0; i < 7; i++) {
            harness.handleMayAbilityChosen(player2, false);
        }
        resolveAllTriggers();

        harness.assertLife(player2, 9);
        harness.assertLife(player1, 41);
    }

    @Test
    void minusEightDiscardIsCausedByOpponentsAbility() {
        addReadyOnyx(player1, 8);
        harness.setLife(player2, 30);
        harness.setHand(player2, List.of(new ObstinateBaloth()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Obstinate Baloth");
        harness.assertNotInGraveyard(player2, "Obstinate Baloth");
        harness.assertLife(player2, 16);
    }

    private Permanent addReadyOnyx(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ProfessorOnyx());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
