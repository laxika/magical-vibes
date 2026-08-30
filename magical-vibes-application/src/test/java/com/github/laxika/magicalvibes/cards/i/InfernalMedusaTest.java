package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({InfernalMedusa.class, AvatarOfMight.class, GiantSpider.class, WallOfWood.class})
class InfernalMedusaTest extends BaseCardTest {

    @Test
    @DisplayName("A non-Wall blocker is destroyed at end of combat")
    void nonWallBlockerDestroyedAtEndOfCombat() {
        Permanent medusa = addReadyMedusa(player1);
        medusa.setAttacking(true);
        addReadyAvatar(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
    }

    @Test
    @DisplayName("A Wall blocker is not destroyed")
    void wallBlockerSurvives() {
        Permanent medusa = addReadyMedusa(player1);
        medusa.setAttacking(true);
        addReadyWall(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wall of Wood");
    }

    @Test
    @DisplayName("A non-Wall attacker blocked by Infernal Medusa is destroyed at end of combat")
    void blockedNonWallAttackerDestroyedAtEndOfCombat() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        addReadyMedusa(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    private Permanent addReadyMedusa(Player player) {
        Permanent perm = new Permanent(new InfernalMedusa());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyAvatar(Player player) {
        Permanent perm = new Permanent(new AvatarOfMight());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyWall(Player player) {
        Permanent perm = new Permanent(new WallOfWood());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
