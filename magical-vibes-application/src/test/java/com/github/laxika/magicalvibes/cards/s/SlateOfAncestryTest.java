package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlateOfAncestry.class, ElvishWarrior.class})
class SlateOfAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for each creature you control and discards your hand as a cost")
    void drawsForEachCreatureAndDiscardsHand() {
        addReadySlate(player1);
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior(), new ElvishWarrior()));

        harness.activateAbility(player1, 0, null, null);

        // Discarding the hand is a cost, so it happens immediately on activation.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        harness.passBothPriorities(); // resolve ability

        // Two creatures controlled -> draw two cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With no creatures, discards hand but draws no cards")
    void noCreaturesDrawsNothing() {
        addReadySlate(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new ElvishWarrior(), new ElvishWarrior()));
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new ElvishWarrior()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can activate with an empty hand")
    void activatesWithEmptyHand() {
        addReadySlate(player1);
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new ElvishWarrior()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // One creature controlled -> draw one card.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counts only creatures controlled by the ability's controller")
    void countsOnlyControlledCreatures() {
        addReadySlate(player1);
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setLibrary(player1, List.of(new ElvishWarrior(), new ElvishWarrior()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without paying the mana cost")
    void requiresManaCost() {
        addReadySlate(player1);
        harness.setHand(player1, List.of(new ElvishWarrior()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void requiresTap() {
        Permanent slate = addReadySlate(player1);
        slate.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player1, List.of(new ElvishWarrior()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySlate(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SlateOfAncestry());
        perm.setSummoningSick(false);
        return perm;
    }
}
