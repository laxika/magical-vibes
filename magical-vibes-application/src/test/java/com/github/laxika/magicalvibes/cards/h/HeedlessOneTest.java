package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeedlessOne.class, ElvishWarrior.class, GoblinSledder.class})
class HeedlessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Heedless One counts itself when it is the only Elf")
    void countsItself() {
        Permanent heedlessOne = addHeedlessOneReady(player1);

        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(1);
    }

    @Test
    @DisplayName("Heedless One counts Elves on both battlefields and ignores other creatures")
    void countsAllElves() {
        Permanent heedlessOne = addHeedlessOneReady(player1);
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.addToBattlefield(player2, new GoblinSledder());

        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(3);
    }

    @Test
    @DisplayName("Heedless One updates its power and toughness as Elves enter and leave")
    void updatesWhenElvesChange() {
        Permanent heedlessOne = addHeedlessOneReady(player1);

        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(3);

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getCard().getName().equals("Elvish Warrior"));
        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, heedlessOne)).isEqualTo(2);
    }

    @Test
    @DisplayName("Heedless One's trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent heedlessOne = addHeedlessOneReady(player1);
        addCreatureReady(player1, new ElvishWarrior());
        Permanent blocker = addCreatureReady(player2, new GoblinSledder());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gqs.getEffectivePower(gd, heedlessOne)).isEqualTo(2);
    }

    private Permanent addHeedlessOneReady(Player player) {
        return addCreatureReady(player, new HeedlessOne());
    }
}
