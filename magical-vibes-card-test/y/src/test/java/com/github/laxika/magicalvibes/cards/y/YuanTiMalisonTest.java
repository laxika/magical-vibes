package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.o.Owlbear;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YuanTiMalison.class, Owlbear.class})
class YuanTiMalisonTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked while attacking alone")
    void cantBeBlockedWhenAttackingAlone() {
        addCreatureReady(player2, new Owlbear());
        Permanent malison = addCreatureReady(player1, new YuanTiMalison());
        malison.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Can be blocked when attacking alongside another creature")
    void canBeBlockedWhenNotAttackingAlone() {
        addCreatureReady(player2, new Owlbear());
        Permanent malison = addCreatureReady(player1, new YuanTiMalison());
        malison.setAttacking(true);
        Permanent companion = addCreatureReady(player1, new Owlbear());
        companion.setAttacking(true);

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Ventures into a dungeon when it deals combat damage to a player")
    void combatDamageMakesControllerVenture() {
        addCreatureReady(player1, new YuanTiMalison());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    void combatDamageAdvancesExistingDungeon() {
        addCreatureReady(player1, new YuanTiMalison());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 1));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
    }

    @Test
    void opponentControlledMalisonVenturesForItsController() {
        addCreatureReady(player2, new YuanTiMalison());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        resolveAllTriggers();
        harness.handleListChoice(player2, "Tomb of Annihilation");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }

    @Test
    void losingAbilitiesAllowsDefenderToDeclareBlockersWhenAttackingAlone() {
        Permanent malison = addCreatureReady(player1, new YuanTiMalison());
        malison.setLosesAllAbilitiesUntilEndOfTurn(true);
        addCreatureReady(player2, new Owlbear());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class))
                .isNotNull();
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not venture when it deals combat damage only to a blocker")
    void blockedDamageDoesNotVenture() {
        addCreatureReady(player1, new YuanTiMalison());
        addCreatureReady(player1, new Owlbear());
        addCreatureReady(player2, new Owlbear());

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
    }
}
