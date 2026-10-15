package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BoskBanneret;
import com.github.laxika.magicalvibes.cards.c.ChameleonColossus;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.r.ReachOfBranches;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MurmuringBosk.class, BoskBanneret.class, Negate.class,
        ReachOfBranches.class, ChameleonColossus.class})
class MurmuringBoskTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you have no Treefolk card in hand")
    void entersTappedWithoutTreefolk() {
        harness.setHand(player1, List.of(new MurmuringBosk(), new Negate()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Revealing a Treefolk lets it enter untapped")
    void entersUntappedWhenRevealing() {
        harness.setHand(player1, List.of(new MurmuringBosk(), new BoskBanneret()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to reveal makes it enter tapped even with a Treefolk in hand")
    void entersTappedWhenDeclining() {
        harness.setHand(player1, List.of(new MurmuringBosk(), new BoskBanneret()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findLand(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green adds {G} and deals no damage")
    void tapForGreen() {
        Permanent land = addLandReady(player1);
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Tapping for white adds {W} and deals 1 damage to controller")
    void tapForWhite() {
        Permanent land = addLandReady(player1);
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Tapping for black adds {B} and deals 1 damage to controller")
    void tapForBlack() {
        Permanent land = addLandReady(player1);
        GameData gd = harness.getGameData();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A noncreature Treefolk card can be revealed and remains in hand")
    void revealKindredInstant() {
        ReachOfBranches treefolk = new ReachOfBranches();
        harness.setHand(player1, List.of(new MurmuringBosk(), treefolk));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(treefolk);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A card with changeling can be revealed as a Treefolk")
    void revealChangeling() {
        ChameleonColossus changeling = new ChameleonColossus();
        harness.setHand(player1, List.of(new MurmuringBosk(), changeling));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(changeling);
    }

    @Test
    @DisplayName("Treefolk outside your hand do not let the land enter untapped")
    void treefolkOutsideOwnHandDoesNotQualify() {
        harness.setHand(player1, List.of(new MurmuringBosk()));
        harness.setHand(player2, List.of(new BoskBanneret()));
        harness.addToBattlefield(player1, new BoskBanneret());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findLand(player1).isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller chooses which eligible Treefolk card to reveal")
    void chooseTreefolkToReveal() {
        BoskBanneret first = new BoskBanneret();
        ReachOfBranches second = new ReachOfBranches();
        harness.setHand(player1, List.of(new MurmuringBosk(), first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleCardChosen(player1, 1);

        assertThat(findLand(player1).isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Black mana damages the land's controller rather than the opponent")
    void blackManaDamagesSecondPlayerController() {
        Permanent land = addLandReady(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addLandReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new MurmuringBosk());
    }

    private Permanent findLand(Player player) {
        return findPermanent(player, "Murmuring Bosk");
    }
}
