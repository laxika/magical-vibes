package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MarchOfOtherworldlyLight;
import com.github.laxika.magicalvibes.cards.m.MoonsnareSpecialist;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PapercraftDecoy.class, WrathOfGod.class, MoonsnareSpecialist.class,
        MarchOfOtherworldlyLight.class})
class PapercraftDecoyTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} after Papercraft Decoy leaves the battlefield draws a card")
    void payingDrawsCard() {
        harness.addToBattlefield(player1, new PapercraftDecoy());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setLibrary(player1, List.of(new PapercraftDecoy()));
        addWrathAndDecoyMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Papercraft Decoy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining the payment does not draw a card")
    void decliningDoesNotDraw() {
        harness.addToBattlefield(player1, new PapercraftDecoy());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setLibrary(player1, List.of(new PapercraftDecoy()));
        addWrathAndDecoyMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Papercraft Decoy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    @DisplayName("Accepting without enough mana does not draw a card")
    void acceptingWithoutEnoughManaDoesNotDraw() {
        harness.addToBattlefield(player1, new PapercraftDecoy());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setLibrary(player1, List.of(new PapercraftDecoy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Papercraft Decoy");
    }

    @Test
    @DisplayName("Returning Decoy to hand triggers and generic payment can use colored mana")
    void returningToHandDrawsExactlyOneCard() {
        PapercraftDecoy decoy = new PapercraftDecoy();
        PapercraftDecoy drawnCard = new PapercraftDecoy();
        Permanent target = harness.addToBattlefieldAndReturn(player1, decoy);
        harness.setHand(player1, List.of(new MoonsnareSpecialist()));
        harness.setLibrary(player1, List.of(drawnCard, new PapercraftDecoy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Papercraft Decoy");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(decoy);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(decoy, drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Exiling an opponent's Decoy lets that opponent pay and draw")
    void exileTriggersForDecoysController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PapercraftDecoy());
        PapercraftDecoy drawnCard = new PapercraftDecoy();
        harness.setHand(player1, List.of(new MarchOfOtherworldlyLight()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(drawnCard, new PapercraftDecoy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantForXWithDiscards(player1, 0, 2, List.of(target.getId()), List.of());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Papercraft Decoy");
        harness.assertNotInGraveyard(player2, "Papercraft Decoy");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card()).contains(target.getCard());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isZero();
    }

    private void addWrathAndDecoyMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
