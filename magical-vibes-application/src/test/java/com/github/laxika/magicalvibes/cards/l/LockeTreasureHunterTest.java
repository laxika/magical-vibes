package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LockeTreasureHunter.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class LockeTreasureHunterTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithGreaterPower() {
        Permanent blocker = addReadyBlocker(new HillGiant());
        Permanent attacker = addReadyLocke();
        attacker.setAttacking(true);

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByCreatureWithEqualPower() {
        Permanent blocker = addReadyBlocker(new GrizzlyBears());
        Permanent attacker = addReadyLocke();
        attacker.setAttacking(true);

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void mugsEachPlayerAndCreatesTreasureForAnyMilledLand() {
        Forest land = new Forest();
        Shock spell = new Shock();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(spell));
        addReadyLocke();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(spell);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.graveyardCastFilterPermissionsThisTurn).hasSize(1);
    }

    @Test
    void mayCastOneMilledSpellFromAnyPlayersGraveyardForItsNormalCost() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(bears));
        harness.setLibrary(player2, List.of(shock));
        addReadyLocke();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromGraveyard(player1, shock.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == shock);
        assertThat(gd.graveyardCastFilterPermissionsThisTurn).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, bears.getId(), null, player2.getId(), List.of(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    private Permanent addReadyLocke() {
        return addCreatureReady(player1, new LockeTreasureHunter());
    }

    private Permanent addReadyBlocker(Card card) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
