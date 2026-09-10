package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FindThePath.class, Forest.class, GrizzlyBears.class})
class FindThePathTest extends BaseCardTest {

    @Test
    @DisplayName("When Find the Path enters, its controller ventures into the dungeon")
    void venturesWhenItEnters() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new FindThePath()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Enchanted land gains a green double-mana ability")
    void enchantedLandGainsDoubleGreenManaAbility() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new FindThePath());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Find the Path cannot target a nonland permanent")
    void cannotTargetNonland() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.setHand(player1, List.of(new FindThePath()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
}
