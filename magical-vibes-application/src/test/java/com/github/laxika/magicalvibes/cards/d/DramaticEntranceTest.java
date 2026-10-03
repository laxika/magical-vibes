package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.cards.t.Tatterkite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DramaticEntrance.class, SafeholdElite.class, Tatterkite.class, BarkshellBlessing.class})
class DramaticEntranceTest extends BaseCardTest {

    private void castEntrance() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Puts a chosen green creature onto the battlefield untapped")
    void putsGreenCreatureUntapped() {
        harness.setHand(player1, List.of(new DramaticEntrance(), new SafeholdElite()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent elite = findPermanent(player1, "Safehold Elite");
        assertThat(elite.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves the creature in hand")
    void decliningLeavesCreatureInHand() {
        harness.setHand(player1, List.of(new DramaticEntrance(), new SafeholdElite()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        harness.assertInHand(player1, "Safehold Elite");
    }

    @Test
    @DisplayName("A non-green creature is not eligible to be put onto the battlefield")
    void nonGreenCreatureIsNotEligible() {
        harness.setHand(player1, List.of(new DramaticEntrance(), new Tatterkite()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Tatterkite");
        harness.assertInHand(player1, "Tatterkite");
    }

    @Test
    @DisplayName("A green noncreature is not eligible to be put onto the battlefield")
    void greenNonCreatureIsNotEligible() {
        harness.setHand(player1, List.of(new DramaticEntrance(), new BarkshellBlessing()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Barkshell Blessing");
        harness.assertInHand(player1, "Barkshell Blessing");
    }

    @Test
    @DisplayName("Chooses one green creature from a mixed hand and leaves the other cards alone")
    void choosesOnlyOneCreatureFromMixedHand() {
        SafeholdElite chosen = new SafeholdElite();
        SafeholdElite remaining = new SafeholdElite();
        Tatterkite colorlessCreature = new Tatterkite();
        BarkshellBlessing greenInstant = new BarkshellBlessing();
        harness.setHand(player1, List.of(new DramaticEntrance(), colorlessCreature,
                chosen, greenInstant, remaining));
        harness.setHand(player2, List.of(new SafeholdElite()));
        castEntrance();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Safehold Elite").getCard()).isSameAs(chosen);
        assertThat(countPermanents(player1, "Safehold Elite")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(
                colorlessCreature, greenInstant, remaining);
        harness.assertInHand(player1, "Tatterkite");
        harness.assertInHand(player1, "Barkshell Blessing");
        harness.assertInHand(player2, "Safehold Elite");
        harness.assertNotOnBattlefield(player2, "Safehold Elite");
        harness.assertInGraveyard(player1, "Dramatic Entrance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Resolves with no creature in hand even when the opponent has one")
    void resolvesWithEmptyHand() {
        harness.setHand(player2, List.of(new SafeholdElite()));
        harness.castFromHand(player1, new DramaticEntrance(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Safehold Elite");
        harness.assertInHand(player2, "Safehold Elite");
        harness.assertInGraveyard(player1, "Dramatic Entrance");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
