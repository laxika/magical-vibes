package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DelversTorch.class, GrizzlyBears.class})
class DelversTorchTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking with the equipped creature makes its controller venture into a dungeon")
    void attackingEquippedCreatureVentureIntoDungeon() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent torch = addTorchReady(player1);
        torch.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("An unattached torch does not trigger when a creature attacks")
    void unattachedTorchDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        addTorchReady(player1);

        declareAttackers(List.of(0));

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Delver's Torch"));
    }

    private Permanent addTorchReady(Player player) {
        Permanent torch = new Permanent(new DelversTorch());
        torch.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(torch);
        return torch;
    }
}
