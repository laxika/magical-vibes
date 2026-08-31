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
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FightRigging.class, Gigantosaurus.class, GrizzlyBears.class})
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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(2));

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
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(imprinted.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
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
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
