package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HulkGammaGoliath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SHIELDHelicarrier.class, HulkGammaGoliath.class})
class SHIELDHelicarrierTest extends BaseCardTest {

    @Test
    @DisplayName("S.H.I.E.L.D. Helicarrier creates two 1/1 white Soldier tokens when it enters")
    void createsTwoSoldiersWhenItEnters() {
        harness.setHand(player1, List.of(new SHIELDHelicarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allMatch(soldier -> soldier.getCard().isToken()
                && soldier.getCard().getPower() == 1
                && soldier.getCard().getToughness() == 1
                && soldier.getCard().getColor() == CardColor.WHITE
                && soldier.getCard().getSubtypes().contains(CardSubtype.SOLDIER));
    }

    @Test
    @DisplayName("Crew 6 animates S.H.I.E.L.D. Helicarrier and taps the crew")
    void crewsHelicarrier() {
        Permanent helicarrier = addHelicarrierReady(player1);
        Permanent crew = addCreatureReady(player1, new HulkGammaGoliath());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(helicarrier.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, helicarrier)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew animation ends at the end of the turn")
    void crewAnimationResetsAtEndOfTurn() {
        Permanent helicarrier = addHelicarrierReady(player1);
        addCreatureReady(player1, new HulkGammaGoliath());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(helicarrier.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, helicarrier)).isFalse();
    }

    @Test
    void newlyEnteredCreatureCanCrew() {
        Permanent helicarrier = harness.addToBattlefieldAndReturn(player1, new SHIELDHelicarrier());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new HulkGammaGoliath());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, helicarrier)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, helicarrier)).isTrue();
    }

    @Test
    void cannotCrewUsingOnlyTheTwoSoldiersItCreates() {
        harness.setHand(player1, List.of(new SHIELDHelicarrier()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Soldier")).allMatch(soldier -> !soldier.isTapped());
        assertThat(gqs.isCreature(gd, findPermanent(player1, "S.H.I.E.L.D. Helicarrier"))).isFalse();
    }

    @Test
    void tappedAndOpposingCreaturesCannotPayCrewCost() {
        Permanent helicarrier = addHelicarrierReady(player1);
        Permanent tappedCrew = addCreatureReady(player1, new HulkGammaGoliath());
        tappedCrew.tap();
        Permanent opposingCrew = addCreatureReady(player2, new HulkGammaGoliath());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opposingCrew.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, helicarrier)).isFalse();
    }

    private Permanent addHelicarrierReady(Player player) {
        return addCreatureReady(player, new SHIELDHelicarrier());
    }
}
