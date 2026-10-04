package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.TheMindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdgarMasterMachinist.class, TheMindStone.class, DarksteelIngot.class,
        GrizzlyBears.class, DarksteelCitadel.class, MindStone.class, SolRing.class})
class EdgarMasterMachinistTest extends BaseCardTest {

    @Test
    @DisplayName("Casts one nonland artifact from the graveyard each turn, and it enters tapped")
    void castsArtifactFromGraveyardTapped() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        TheMindStone mindStone = new TheMindStone();
        harness.setGraveyard(player1, List.of(mindStone, new DarksteelIngot()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        prepareMainPhase(player1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        Permanent mindStonePermanent = findPermanent(player1, "The Mind Stone");
        assertThat(mindStonePermanent.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not cast nonartifact or artifact land cards from the graveyard")
    void rejectsNonArtifactAndArtifactLandCards() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new DarksteelCitadel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.playLandFromGraveyard(player1, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gets +X/+0 on attack for the greatest artifact mana value")
    void boostsByGreatestArtifactManaValue() {
        Permanent edgar = addCreatureReady(player1, new EdgarMasterMachinist());
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new TheMindStone());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(edgar.getPowerModifier()).isEqualTo(3);
        assertThat(edgar.getToughnessModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(edgar.getPowerModifier()).isZero();
    }

    @Test
    void requiresManaAndDoesNotSpendPermissionOnFailedCast() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        harness.setGraveyard(player1, List.of(new MindStone()));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mind Stone").isTapped()).isTrue();
    }

    @Test
    void artifactStillEntersTappedAfterEdgarLeaves() {
        Permanent edgar = harness.addToBattlefieldAndReturn(player1, new EdgarMasterMachinist());
        harness.setGraveyard(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(edgar);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mind Stone").isTapped()).isTrue();
    }

    @Test
    void doesNotGrantPermissionToOpponent() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        harness.setGraveyard(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotOverrideNormalArtifactTiming() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        harness.setGraveyard(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionRefreshesOnNextTurn() {
        harness.addToBattlefield(player1, new EdgarMasterMachinist());
        harness.setGraveyard(player1, List.of(new MindStone(), new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase(player1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sol Ring").isTapped()).isTrue();
    }

    @Test
    void attackBonusIgnoresOpponentsArtifacts() {
        Permanent edgar = addCreatureReady(player1, new EdgarMasterMachinist());
        harness.addToBattlefield(player2, new MindStone());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(edgar.getPowerModifier()).isZero();
        assertThat(edgar.getToughnessModifier()).isZero();
    }

    @Test
    void attackBonusUsesArtifactsAtResolutionAndThenRemainsFixed() {
        Permanent edgar = addCreatureReady(player1, new EdgarMasterMachinist());
        harness.addToBattlefield(player1, new SolRing());
        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isNotEmpty();
        harness.addToBattlefield(player1, new MindStone());

        resolveAllTriggers();
        assertThat(edgar.getPowerModifier()).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof MindStone);
        assertThat(edgar.getPowerModifier()).isEqualTo(2);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
