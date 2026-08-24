package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThaliaAndTheGitrogMonster.class, FieldOfRuin.class, Forest.class, GrizzlyBears.class})
class ThaliaAndTheGitrogMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can play two lands in one turn")
    void controllerCanPlayTwoLands() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Forest".equals(permanent.getCard().getName()))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponents' creatures and nonbasic lands enter tapped")
    void opponentsCreaturesAndNonbasicLandsEnterTapped() {
        harness.addToBattlefield(player1, new ThaliaAndTheGitrogMonster());
        harness.setHand(player2, List.of(new GrizzlyBears(), new FieldOfRuin()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        gs.playCard(gd, player2, 0, 0, null, null);

        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player2, "Field of Ruin").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking sacrifices a chosen creature or land, then draws a card")
    void attackingSacrificesChosenPermanentThenDraws() {
        addReadyPermanent(player1, new ThaliaAndTheGitrogMonster());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
