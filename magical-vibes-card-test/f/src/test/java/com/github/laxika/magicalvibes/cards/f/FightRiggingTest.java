package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.Gigantosaurus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FightRigging.class, Forest.class, Gigantosaurus.class, GrizzlyBears.class})
class FightRiggingTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway exiles one of the top five cards face down and bottoms the rest")
    void hideawayExilesOneCard() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        Card chosen = library.get(2);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new FightRigging()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 2);

        Permanent fightRigging = findPermanent(player1, "Fight Rigging");
        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(fightRigging.getCard())).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(chosen);
    }

    @Test
    @DisplayName("Beginning of combat puts a counter on a target creature and offers the imprinted card with a large creature")
    void counterAndFreePlayWithPowerSevenCreature() {
        Card imprinted = new GrizzlyBears();
        addFightRiggingWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new Gigantosaurus());

        resolveBeginningOfCombat(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(imprinted.getId())).isNull();
    }

    @Test
    @DisplayName("Beginning of combat still puts the counter on a target when no creature has power seven")
    void noFreePlayBelowPowerThreshold() {
        Card imprinted = new GrizzlyBears();
        addFightRiggingWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        resolveBeginningOfCombat(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(target);
    }

    @Test
    @DisplayName("Hideaway exiles the only card when the library contains fewer than five cards")
    void hideawayWithSingleCardLibrary() {
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen));
        harness.setHand(player1, List.of(new FightRigging()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, "Fight Rigging");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(chosen.getId())).isNotNull();
        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(source.getCard())).isSameAs(chosen);
    }

    @Test
    @DisplayName("The counter can raise a creature from six power to seven before the condition is checked")
    void counterEnablesFreePlay() {
        Card imprinted = new GrizzlyBears();
        addFightRiggingWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        resolveBeginningOfCombat(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(gd.findExiledCard(imprinted.getId())).isNull();
    }

    @Test
    @DisplayName("Declining the free play leaves the card in exile and keeps the counter")
    void mayDeclineFreePlay() {
        Card imprinted = new GrizzlyBears();
        Permanent source = addFightRiggingWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new Gigantosaurus());

        resolveBeginningOfCombat(target);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        assertThat(gd.getImprintedCard(source.getCard())).isSameAs(imprinted);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's large creature does not enable free play")
    void opponentPowerDoesNotSatisfyCondition() {
        Card imprinted = new GrizzlyBears();
        addFightRiggingWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new Gigantosaurus());

        resolveBeginningOfCombat(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(target);
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("A land may be played during combat when a land play is available")
    void playsExiledLandDuringCombat() {
        Card imprinted = new Forest();
        addFightRiggingWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new Gigantosaurus());

        resolveBeginningOfCombat(target);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(imprinted.getId())).isNull();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Free play cannot exceed the turn's land play limit")
    void cannotPlayExiledLandAfterUsingLandPlay() {
        Card imprinted = new Forest();
        Permanent source = addFightRiggingWithImprint(imprinted);
        Permanent target = addCreatureReady(player1, new Gigantosaurus());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        resolveBeginningOfCombat(target);
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        assertThat(gd.getImprintedCard(source.getCard())).isSameAs(imprinted);
    }

    private Permanent addFightRiggingWithImprint(Card imprinted) {
        harness.addToBattlefield(player1, new FightRigging());
        Permanent fightRigging = findPermanent(player1, "Fight Rigging");
        GameData gameData = harness.getGameData();
        gameData.setImprintedCard(fightRigging.getCard(), imprinted);
        gameData.addToExile(player1.getId(), imprinted);
        return fightRigging;
    }

    private void resolveBeginningOfCombat(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
