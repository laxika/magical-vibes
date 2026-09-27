package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.r.RendFlesh;
import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmberFistZubera.class, IsamaruHoundOfKonda.class, RendFlesh.class, RendSpirit.class})
class EmberFistZuberaTest extends BaseCardTest {

    // "When this creature dies, it deals damage to any target equal to the number of Zubera that died this turn."

    private void startMainPhase(Card... spells) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(spells));
        harness.addMana(player1, ManaColor.BLACK, 3 * spells.length);
    }

    @Test
    @DisplayName("Dies alone: deals 1 damage to the chosen player")
    void diesAloneDealsOne() {
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.setLife(player2, 20);
        startMainPhase(new RendSpirit());

        harness.castAndResolveInstant(player1, 0, zubera.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Counts every Zubera that died this turn, including itself")
    void countsAllZuberaDeathsThisTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.setLife(player2, 20);
        startMainPhase(new RendSpirit(), new RendSpirit());

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);

        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Counts Zubera deaths under either player's control")
    void countsZuberaDeathsAcrossPlayers() {
        Permanent ownZubera = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        Permanent opposingZubera = harness.addToBattlefieldAndReturn(player2, new EmberFistZubera());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        startMainPhase(new RendSpirit(), new RendSpirit());

        harness.castAndResolveInstant(player1, 0, opposingZubera.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 19);

        harness.castAndResolveInstant(player1, 0, ownZubera.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Non-Zubera deaths do not increase the damage")
    void nonZuberaDeathsDoNotCount() {
        Permanent nonZubera = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent zubera = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.setLife(player2, 20);
        startMainPhase(new RendFlesh(), new RendSpirit());

        harness.castAndResolveInstant(player1, 0, nonZubera.getId());

        harness.castAndResolveInstant(player1, 0, zubera.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Damage can be aimed at a creature and kills it when lethal")
    void damageCanKillCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EmberFistZubera());
        harness.addToBattlefield(player2, new IsamaruHoundOfKonda());
        UUID isamaruId = harness.getPermanentId(player2, "Isamaru, Hound of Konda");
        startMainPhase(new RendSpirit(), new RendSpirit());

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.handlePermanentChosen(player1, isamaruId);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(isamaruId));

        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.handlePermanentChosen(player1, isamaruId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
    }
}
