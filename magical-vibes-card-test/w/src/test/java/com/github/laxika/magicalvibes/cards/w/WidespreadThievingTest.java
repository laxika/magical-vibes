package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Goldhound;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WidespreadThieving.class, Goldhound.class, CivilServant.class, Forest.class})
class WidespreadThievingTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway 5 exiles one card face down and puts the rest on the bottom")
    void hideawayExilesOneCardAndBottomsTheRest() {
        Card chosen = new Goldhound();
        Card second = new Goldhound();
        Card third = new Goldhound();
        Card fourth = new Goldhound();
        Card fifth = new Goldhound();
        harness.setLibrary(player1, List.of(chosen, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new WidespreadThieving()));
        addWidespreadMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent thieving = findPermanent(player1, "Widespread Thieving");
        ExiledCardEntry exiled = gd.findExiledCard(chosen.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(thieving.getCard())).isSameAs(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A multicolored spell creates a Treasure and can pay to play the exiled card")
    void multicoloredSpellCreatesTreasureAndPaysToPlayExiledCard() {
        addThievingWithImprint(new Goldhound());
        harness.setHand(player1, List.of(new CivilServant()));
        addCivilServantAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countTreasures()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Goldhound");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Goldhound"));
    }

    @Test
    @DisplayName("Declining the payment keeps the imprinted card exiled")
    void decliningPaymentKeepsImprintedCardExiled() {
        Card imprinted = new Goldhound();
        addThievingWithImprint(imprinted);
        harness.setHand(player1, List.of(new CivilServant()));
        addCivilServantAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countTreasures()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(imprinted);
        harness.assertNotOnBattlefield(player1, "Goldhound");
    }

    @Test
    @DisplayName("A monocolored spell does not create Treasure or offer payment")
    void monocoloredSpellDoesNotTrigger() {
        Card imprinted = new Goldhound();
        addThievingWithImprint(imprinted);
        harness.setHand(player1, List.of(new Goldhound()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countTreasures()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(imprinted);
    }

    @Test
    @DisplayName("Paying five colors still allows declining to play the exiled card")
    void canDeclinePlayingAfterPaying() {
        Card imprinted = new Goldhound();
        addThievingWithImprint(imprinted);
        harness.setHand(player1, List.of(new CivilServant()));
        addCivilServantAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countTreasures()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(imprinted);
        harness.assertNotOnBattlefield(player1, "Goldhound");
    }

    @Test
    @DisplayName("Hideaway exiles the only card in a short library without a choice")
    void hideawayWithOneCard() {
        Card chosen = new Goldhound();
        harness.setLibrary(player1, List.of(chosen));
        harness.setHand(player1, List.of(new WidespreadThieving()));
        addWidespreadMana();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(chosen.getId()).faceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getImprintedCard(findPermanent(player1, "Widespread Thieving").getCard()))
                .isSameAs(chosen);
    }

    @Test
    @DisplayName("An exiled land may be played when a land play remains")
    void canPlayExiledLand() {
        Card imprinted = new Forest();
        addThievingWithImprint(imprinted);
        harness.setHand(player1, List.of(new CivilServant()));
        addCivilServantAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(imprinted);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("An exiled land cannot be played after using the turn's land play")
    void cannotPlayExiledLandAfterLandPlay() {
        Card imprinted = new Forest();
        addThievingWithImprint(imprinted);
        harness.setHand(player1, List.of(new Forest(), new CivilServant()));
        harness.playLand(player1, 0);
        addCivilServantAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(imprinted);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(imprinted.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's multicolored spell does not trigger Thieving")
    void opponentMulticoloredSpellDoesNotTrigger() {
        addThievingWithImprint(new Goldhound());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CivilServant()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(countTreasures()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertOnBattlefield(player2, "Civil Servant");
    }

    @Test
    @DisplayName("Playing an exiled multicolored spell triggers Thieving again")
    void playingExiledMulticoloredSpellTriggersAgain() {
        Card imprinted = new CivilServant();
        addThievingWithImprint(imprinted);
        harness.setHand(player1, List.of(new CivilServant()));
        addCivilServantAndWidespreadMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(countTreasures()).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(imprinted);
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Civil Servant");
    }

    private void addThievingWithImprint(Card imprinted) {
        Permanent thieving = harness.addToBattlefieldAndReturn(player1, new WidespreadThieving());
        gd.setImprintedCard(thieving.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
    }

    private void addWidespreadMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addCivilServantAndWidespreadMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private int countTreasures() {
        return (int) gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Treasure"))
                .count();
    }
}
