package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GitrogHorrorOfZhava.class, GrizzlyBears.class, FieldOfRuin.class, Forest.class})
class GitrogHorrorOfZhavaTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void opponentMaySacrificeNontokenCreatureToTapGitrogAndSeekLand() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new FieldOfRuin()));

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gitrog.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent soughtLand = findPermanent(player1, "Field of Ruin");
        assertThat(soughtLand.isTapped()).isTrue();
    }

    @Test
    void tappedGitrogDoesNotOfferCombatChoice() {
        Permanent gitrog = harness.addToBattlefieldAndReturn(player1, new GitrogHorrorOfZhava());
        gitrog.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void landfallPerpetuallyGrantsTheSacrificeDrawAbility() {
        harness.addToBattlefield(player1, new GitrogHorrorOfZhava());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        Permanent forest = harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();

        assertThat(gs.getEffectiveActivatedAbilities(gd, forest)).hasSize(1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        forest.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest), null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
