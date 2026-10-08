package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DragonsFire;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZaltoFireGiantDuke.class, ProdigalSorcerer.class, DragonsFire.class})
class ZaltoFireGiantDukeTest extends BaseCardTest {

    @Test
    @DisplayName("When Zalto is dealt damage, its controller ventures into the dungeon")
    void dealtDamageVenturesIntoDungeon() {
        Permanent zalto = harness.addToBattlefieldAndReturn(player1, new ZaltoFireGiantDuke());
        Permanent pinger = addCreatureReady(player1, new ProdigalSorcerer());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(pinger), null,
                zalto.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("Lethal damage still lets Zalto's controller choose and enter a dungeon")
    void lethalDamageStillVentures() {
        Permanent zalto = harness.addToBattlefieldAndReturn(player2, new ZaltoFireGiantDuke());
        harness.setHand(player1, List.of(new DragonsFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, zalto.getId());
        harness.assertInGraveyard(player2, "Zalto, Fire Giant Duke");
        resolveAllTriggers();
        harness.handleListChoice(player2, "Dungeon of the Mad Mage");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.DUNGEON_OF_THE_MAD_MAGE, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Each separate damage event advances the existing dungeon once")
    void separateDamageEventsAdvanceDungeon() {
        Permanent zalto = harness.addToBattlefieldAndReturn(player1, new ZaltoFireGiantDuke());
        Permanent first = addCreatureReady(player2, new ProdigalSorcerer());
        Permanent second = addCreatureReady(player2, new ProdigalSorcerer());
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(first),
                null, zalto.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "Goblin Lair");
        resolveAllTriggers();
        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(second),
                null, zalto.getId());
        resolveAllTriggers();
        harness.handleListChoice(player1, "Dark Pool");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 4));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player2.getId());
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Combat damage to Zalto triggers a venture for its controller")
    void combatDamageVentures() {
        Permanent zalto = addCreatureReady(player2, new ZaltoFireGiantDuke());
        Permanent attacker = addCreatureReady(player1, new ProdigalSorcerer());
        attacker.setAttacking(true);
        zalto.setBlocking(true);
        zalto.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();
        harness.handleListChoice(player2, "Tomb of Annihilation");
        resolveAllTriggers();

        assertThat(gd.playerDungeonProgress.get(player2.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.TOMB_OF_ANNIHILATION, 0));
        assertThat(gd.playerDungeonProgress).doesNotContainKey(player1.getId());
        harness.assertOnBattlefield(player2, "Zalto, Fire Giant Duke");
        harness.assertInGraveyard(player1, "Prodigal Sorcerer");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
