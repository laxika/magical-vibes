package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.f.FaerieTauntings;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NectarFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SecludedGlen.class, Forest.class, NectarFaerie.class, FaerieTauntings.class, CribSwap.class,
        Spelunking.class})
class SecludedGlenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Faerie card in hand")
    void entersTappedWithoutFaerie() {
        harness.setHand(player1, List.of(new SecludedGlen(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Faerie lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new SecludedGlen(), new NectarFaerie()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with a Faerie in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new SecludedGlen(), new NectarFaerie()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent land = findLand(player1);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new SecludedGlen());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new SecludedGlen());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A noncreature Faerie card can be revealed and remains in hand")
    void revealsKindredFaerieEnchantment() {
        harness.setHand(player1, List.of(new SecludedGlen(), new FaerieTauntings()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        harness.assertInHand(player1, "Faerie Tauntings");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A noncreature card with changeling can be revealed as a Faerie")
    void revealsChangelingInstant() {
        harness.setHand(player1, List.of(new SecludedGlen(), new CribSwap()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        harness.assertInHand(player1, "Crib Swap");
    }

    @Test
    @DisplayName("An opponent's Faerie in hand cannot be revealed")
    void opponentsFaerieDoesNotAllowUntappedEntry() {
        harness.setHand(player1, List.of(new SecludedGlen()));
        harness.setHand(player2, List.of(new NectarFaerie()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Faerie on the battlefield cannot be revealed from hand")
    void battlefieldFaerieDoesNotAllowUntappedEntry() {
        harness.setHand(player1, List.of(new SecludedGlen()));
        harness.addToBattlefield(player1, new NectarFaerie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("A Faerie in the graveyard cannot be revealed from hand")
    void graveyardFaerieDoesNotAllowUntappedEntry() {
        harness.setHand(player1, List.of(new SecludedGlen()));
        harness.setGraveyard(player1, List.of(new NectarFaerie()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Spelunking allows choosing untapped entry without revealing a Faerie")
    void canChooseUntappedEntryWithSpelunkingWithoutFaerie() {
        harness.addToBattlefield(player1, new Spelunking());
        harness.setHand(player1, List.of(new SecludedGlen()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.pendingInteractions).anySatisfy(interaction ->
                assertThat(interaction).isInstanceOf(PendingInteraction.ColorChoice.class));
        harness.handleListChoice(player1, "Untapped");

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Secluded Glen");
    }
}
