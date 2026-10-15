package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.cards.s.SiegeWurm;
import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hypothesizzle.class, Forest.class, VernadiShieldmate.class, SiegeWurm.class, UnexplainedDisappearance.class})
class HypothesizzleTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discarding a nonland card deals 4 damage to a creature")
    void drawsAndDealsDamageAfterDiscarding() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new VernadiShieldmate(), new Forest()));
        harness.addToBattlefield(player2, new VernadiShieldmate());
        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        int nonlandIndex = findCardIndex(player1, "Vernadi Shieldmate");
        harness.handleCardChosen(player1, nonlandIndex);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vernadi Shieldmate");
        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the discard still keeps the two drawn cards and deals no damage")
    void decliningDiscardDealsNoDamage() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player2, new VernadiShieldmate());
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Vernadi Shieldmate");
    }

    @Test
    @DisplayName("The discard choice offers nonland cards but not lands")
    void onlyOffersNonlandCards() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new Forest(), new VernadiShieldmate()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice choice = (PendingInteraction.DiscardChoice) gd.interaction.activeInteraction();
        int landIndex = findCardIndex(player1, "Forest");
        int nonlandIndex = findCardIndex(player1, "Vernadi Shieldmate");
        assertThat(choice.validIndices()).contains(nonlandIndex).doesNotContain(landIndex);
    }


    @Test
    @DisplayName("Accepting with only lands in hand finishes without discarding or dealing damage")
    void cannotDiscardWhenOnlyLandsWereDrawn() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player2, new VernadiShieldmate());
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Vernadi Shieldmate");
        harness.assertInGraveyard(player1, "Hypothesizzle");
    }

    @Test
    @DisplayName("A nonland card may be discarded even when there is no creature to target")
    void canDiscardWithoutAnyCreature() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new VernadiShieldmate(), new Forest()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, findCardIndex(player1, "Vernadi Shieldmate"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        harness.assertInGraveyard(player1, "Hypothesizzle");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A separate damage trigger can target your own creature and deals exactly four damage")
    void dealsExactlyFourDamageToOwnCreature() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new VernadiShieldmate(), new Forest()));
        harness.addToBattlefield(player1, new SiegeWurm());
        UUID targetId = harness.getPermanentId(player1, "Siege Wurm");
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, findCardIndex(player1, "Vernadi Shieldmate"));
        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Hypothesizzle");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isZero();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Siege Wurm");
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getMarkedDamage()).isEqualTo(4);
    }


    @Test
    @DisplayName("Responding to the damage trigger by returning its target does not undo the discard")
    void removingTargetInResponseLeavesDiscardAndDrawsIntact() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new VernadiShieldmate(), new Forest()));
        harness.setHand(player2, List.of(new UnexplainedDisappearance()));
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player2, new VernadiShieldmate());
        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, findCardIndex(player1, "Vernadi Shieldmate"));
        harness.handlePermanentChosen(player1, targetId);

        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player2, "Vernadi Shieldmate");
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Vernadi Shieldmate");
        harness.assertNotInGraveyard(player2, "Vernadi Shieldmate");
        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        harness.assertInGraveyard(player1, "Hypothesizzle");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int findCardIndex(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return java.util.stream.IntStream.range(0, gd.playerHands.get(player.getId()).size())
                .filter(i -> gd.playerHands.get(player.getId()).get(i).getName().equals(cardName))
                .findFirst()
                .orElseThrow();
    }
}
