package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({LockeTreasureHunter.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        LeylineOfTheVoid.class})
class LockeTreasureHunterTest extends BaseCardTest {

    @Test
    void cannotBeBlockedByCreatureWithGreaterPower() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent attacker = addReadyLocke();
        attacker.setAttacking(true);

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByCreatureWithEqualPower() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
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
        harness.ensurePriority(player1);
        gs.playFlashbackSpell(gd, player1, shock.getId(), null, player2.getId(), List.of(), null, null);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == shock);
        assertThat(gd.graveyardCastFilterPermissionsThisTurn).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, bears.getId(), null, null, List.of(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    @Test
    void createsOnlyOneTreasureWhenBothPlayersMillLands() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        addReadyLocke();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void createsNoTreasureWhenNeitherPlayerMillsALand() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Shock()));
        addReadyLocke();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsTreasureWhenMilledLandIsExiledInsteadOfEnteringGraveyard() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLibrary(player2, List.of(land));
        addReadyLocke();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(land);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void mayCastMilledSpellFromExileWhenGraveyardEntryIsReplaced() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(shock));
        addReadyLocke();
        harness.addToBattlefield(player1, new LeylineOfTheVoid());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == shock);
    }

    @Test
    void cannotCastCreatureDuringCombatButCanCastItInMainPhase() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setLibrary(player2, List.of(new Shock()));
        addReadyLocke();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromGraveyard(player1, bears.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == bears);
    }

    @Test
    void cannotCastAnUnrelatedCardAlreadyInGraveyard() {
        Shock unrelated = new Shock();
        harness.setGraveyard(player1, List.of(unrelated));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Shock()));
        addReadyLocke();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, unrelated.getId(), null, player2.getId(), List.of(), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be cast from graveyard");
    }

    private Permanent addReadyLocke() {
        return addCreatureReady(player1, new LockeTreasureHunter());
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
