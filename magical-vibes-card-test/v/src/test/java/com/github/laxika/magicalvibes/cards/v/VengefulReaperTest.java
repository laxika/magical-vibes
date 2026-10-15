package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.cards.r.RavenousLindwurm;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengefulReaper.class, RavenousLindwurm.class})
class VengefulReaperTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack on the turn it enters")
    void hasteAllowsImmediateAttack() {
        harness.castFromHand(player1, new VengefulReaper(), "{3}{B}");
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void groundCreatureCannotBlockFlyingReaper() {
        harness.addToBattlefield(player1, new VengefulReaper());
        addCreatureReady(player2, new RavenousLindwurm());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deathtouchKillsLargerAttackerWhenBlocking() {
        addCreatureReady(player1, new RavenousLindwurm());
        harness.addToBattlefield(player2, new VengefulReaper());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Ravenous Lindwurm");
        harness.assertInGraveyard(player2, "Vengeful Reaper");
    }

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(reaper.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, reaper.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vengeful Reaper");
    }

    @Test
    void cannotCastOnTurnItWasForetold() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, reaper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(reaper.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellDuringOpponentsTurn() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(reaper);
        assertThat(gd.findExiledCard(reaper.getId())).isNull();
    }

    @Test
    void canForetellDuringCombatWithoutPuttingSpellOnStack() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(reaper.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotForetellWithOnlyOneMana() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(reaper);
        assertThat(gd.findExiledCard(reaper.getId())).isNull();
    }

    @Test
    void foretellCostStillRequiresBlackMana() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, reaper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(reaper.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foretoldCreatureStillRequiresSorceryTiming() {
        VengefulReaper reaper = new VengefulReaper();
        harness.setHand(player1, List.of(reaper));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromExile(player1, reaper.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(reaper.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }
}
