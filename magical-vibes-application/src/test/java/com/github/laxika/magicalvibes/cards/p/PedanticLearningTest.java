package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.cards.v.VexingArcanix;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PedanticLearning.class, Millstone.class, Forest.class, GrizzlyBears.class,
        StoneRain.class, VexingArcanix.class})
class PedanticLearningTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {1} to draw when a land is put into the graveyard from the library")
    void paysToDrawWhenLandIsMilled() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllChoices(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Declining the payment does not draw")
    void decliningPaymentDoesNotDraw() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Millstone());
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), grizzlyBears));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllChoices(false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(grizzlyBears);
    }

    @Test
    @DisplayName("Two milled lands create two separate draw opportunities")
    void eachMilledLandCreatesItsOwnTrigger() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllChoices(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.hasType(CardType.LAND))
                .hasSize(2);
    }

    @Test
    @DisplayName("Milling nonland cards does not trigger")
    void nonlandCardsDoNotTrigger() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllChoices(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("An opponent milling your land still triggers Pedantic Learning")
    void opponentMillingYourLandStillTriggers() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player2, new Millstone());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllChoices(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("A land put into the graveyard from the library without milling still triggers")
    void nonMillLibraryToGraveyardStillTriggers() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new VexingArcanix());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vexing Arcanix");
        resolveAllChoices(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Milling an opponent's land does not trigger your Pedantic Learning")
    void opponentsMilledLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player2, new Millstone());
        harness.setLibrary(player2, List.of(new Forest(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player2, 0, null, player2.getId());
        resolveAllChoices(true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("A land entering the graveyard from the battlefield does not trigger")
    void battlefieldLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new PedanticLearning());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, harness.getPermanentId(player1, "Forest"));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isNotEmpty();
    }

    private void resolveAllChoices(boolean acceptPayment) {
        int guard = 0;
        while ((!gd.stack.isEmpty() || gd.interaction.activeInteraction() != null) && guard++ < 50) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, acceptPayment);
            } else {
                harness.passBothPriorities();
            }
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
