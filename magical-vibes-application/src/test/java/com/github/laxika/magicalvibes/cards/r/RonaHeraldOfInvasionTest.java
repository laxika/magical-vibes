package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        RonaHeraldOfInvasion.class,
        RonaTolarianObliterator.class,
        Forest.class,
        GrizzlyBears.class,
        Shock.class
})
class RonaHeraldOfInvasionTest extends BaseCardTest {

    @Test
    void tapAbilityLoots() {
        Permanent rona = addReadyRona();
        Shock drawn = new Shock();
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(rona.isTapped()).isTrue();
    }

    @Test
    void legendarySpellUntapsRona() {
        Permanent rona = addReadyRona();
        rona.tap();

        RonaHeraldOfInvasion legendarySpell = new RonaHeraldOfInvasion();
        harness.setHand(player1, List.of(legendarySpell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(rona.isTapped()).isFalse();
    }

    @Test
    void transformsWithPhyrexianManaAtSorcerySpeed() {
        Permanent rona = addReadyRona();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(rona.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void transformedRonaMayPutExiledLandOntoBattlefieldUnderItsController() {
        Permanent rona = transformRona();
        Shock shock = new Shock();
        Forest forest = new Forest();
        harness.setHand(player2, List.of(shock, forest));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castInstant(player2, 0, rona.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void transformedRonaMayCastExiledNonlandWithoutPayingManaCost() {
        Permanent rona = transformRona();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(shock, bears));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase(player2);

        harness.castInstant(player2, 0, rona.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent -> permanent.getCard() == bears);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private Permanent addReadyRona() {
        Permanent rona = harness.addToBattlefieldAndReturn(player1, new RonaHeraldOfInvasion());
        rona.setSummoningSick(false);
        return rona;
    }

    private Permanent transformRona() {
        Permanent rona = addReadyRona();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(rona.isTransformed()).isTrue();
        return rona;
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
