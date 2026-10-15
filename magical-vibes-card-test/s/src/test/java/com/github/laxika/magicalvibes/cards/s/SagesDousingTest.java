package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.i.InkDissolver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SagesDousing.class, InkDissolver.class, ElvishWarrior.class})
class SagesDousingTest extends BaseCardTest {

    private void stockLibrary(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new ElvishWarrior());
        }
        harness.setLibrary(player, deck);
    }

    private void castWarriorCounteredBy(SagesDousing dousing) {
        ElvishWarrior warrior = new ElvishWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(dousing));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, warrior.getId());
    }

    // ===== Counter branch =====

    @Test
    @DisplayName("Counters the target spell when its controller cannot pay {3}")
    void countersWhenControllerCannotPay() {
        SagesDousing dousing = new SagesDousing();
        castWarriorCounteredBy(dousing);

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when its controller pays {3}")
    void notCounteredWhenControllerPays() {
        SagesDousing dousing = new SagesDousing();
        ElvishWarrior warrior = new ElvishWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 5); // 2 to cast, 3 to pay

        harness.setHand(player2, List.of(dousing));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Elvish Warrior");
    }

    @Test
    @DisplayName("Draws a card after the target controller pays when you control a Wizard")
    void drawsAfterTargetControllerPaysWhenControllingWizard() {
        harness.addToBattlefield(player2, new InkDissolver());
        stockLibrary(player2, 1);

        SagesDousing dousing = new SagesDousing();
        ElvishWarrior warrior = new ElvishWarrior();
        harness.setHand(player1, List.of(warrior));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.setHand(player2, List.of(dousing));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    // ===== Conditional draw =====

    @Test
    @DisplayName("Draws a card if you control a Wizard")
    void drawsWhenControllingWizard() {
        harness.addToBattlefield(player2, new InkDissolver());
        stockLibrary(player2, 3);

        SagesDousing dousing = new SagesDousing();
        castWarriorCounteredBy(dousing);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not draw if you control no Wizard")
    void noDrawWithoutWizard() {
        stockLibrary(player2, 3);

        SagesDousing dousing = new SagesDousing();
        castWarriorCounteredBy(dousing);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's Wizard does not enable the draw")
    void noDrawForOpponentsWizard() {
        harness.addToBattlefield(player1, new InkDissolver());
        stockLibrary(player2, 3);

        castWarriorCounteredBy(new SagesDousing());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Controlling multiple Wizards still draws exactly one card")
    void multipleWizardsDrawOnlyOneCard() {
        harness.addToBattlefield(player2, new InkDissolver());
        harness.addToBattlefield(player2, new InkDissolver());
        stockLibrary(player2, 3);

        castWarriorCounteredBy(new SagesDousing());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining an affordable payment counters the spell and still draws")
    void drawsWhenControllerDeclinesPayment() {
        harness.addToBattlefield(player2, new InkDissolver());
        stockLibrary(player2, 3);
        castWarriorCounteredBy(new SagesDousing());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }
}
