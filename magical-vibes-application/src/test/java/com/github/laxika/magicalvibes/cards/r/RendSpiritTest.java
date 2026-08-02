package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RendSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Spirit")
    void destroysTargetSpirit() {
        Permanent spirit = new Permanent(new KamiOfOldStone());
        gd.playerBattlefields.get(player2.getId()).add(spirit);
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Kami of Old Stone"));
    }

    @Test
    @DisplayName("Cannot target a non-Spirit creature")
    void cannotTargetNonSpirit() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new KamiOfOldStone()));
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Spirit");
    }
}
