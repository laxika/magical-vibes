package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NahiriTheHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("+2 may discard a card and draw a card")
    void plusTwoDiscardsAndDraws() {
        Permanent nahiri = addReadyNahiri(player1, 3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(nahiri.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("-2 exiles an enchantment")
    void minusTwoExilesEnchantment() {
        addReadyNahiri(player1, 3);
        harness.addToBattlefield(player2, new GloriousAnthem());
        var anthemId = harness.getPermanentId(player2, "Glorious Anthem");

        harness.activateAbility(player1, 0, 1, null, anthemId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Glorious Anthem"));
    }

    @Test
    @DisplayName("-2 exiles a tapped artifact")
    void minusTwoExilesTappedArtifact() {
        addReadyNahiri(player1, 3);
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        mindStone.tap();

        harness.activateAbility(player1, 0, 1, null, mindStone.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("-2 exiles a tapped creature")
    void minusTwoExilesTappedCreature() {
        addReadyNahiri(player1, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.tap();

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 cannot target an untapped artifact")
    void minusTwoCannotTargetUntappedArtifact() {
        addReadyNahiri(player1, 3);
        harness.addToBattlefield(player2, new MindStone());
        var mindStoneId = harness.getPermanentId(player2, "Mind Stone");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, mindStoneId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-8 puts an artifact or creature onto the battlefield with haste, then returns it to hand")
    void minusEightReturnsFoundPermanentToHandAtNextEndStep() {
        addReadyNahiri(player1, 8);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(new ShivanDragon(), new Forest()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Shivan Dragon");
        Permanent dragon = findPermanent(player1, "Shivan Dragon");
        assertThat(dragon.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        harness.assertInHand(player1, "Shivan Dragon");
    }

    private Permanent addReadyNahiri(Player player, int loyalty) {
        NahiriTheHarbinger card = new NahiriTheHarbinger();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
