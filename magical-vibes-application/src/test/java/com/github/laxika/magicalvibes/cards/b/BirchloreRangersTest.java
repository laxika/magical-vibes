package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BirchloreRangers.class, ElvishWarrior.class, GrizzlyBears.class})
class BirchloreRangersTest extends BaseCardTest {

    @Test
    void tapsTwoElvesAndAddsManaOfChosenColor() {
        Permanent source = addCreatureReady(player1, new BirchloreRangers());
        Permanent elf = addCreatureReady(player1, new ElvishWarrior());
        Permanent nonElf = addCreatureReady(player1, new GrizzlyBears());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        harness.activateAbility(player1, sourceIndex, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(source.isTapped()).isTrue();
        assertThat(elf.isTapped()).isTrue();
        assertThat(nonElf.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutTwoUntappedElves() {
        Permanent source = addCreatureReady(player1, new BirchloreRangers());
        addCreatureReady(player1, new GrizzlyBears());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }
}
