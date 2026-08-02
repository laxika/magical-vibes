package com.github.laxika.magicalvibes.cards.d;

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

class DevouringGreedTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Drains 2 life plus 2 for each Spirit sacrificed")
    void drainsTwoPlusTwoPerSacrificedSpirit() {
        Permanent first = new Permanent(new KamiOfOldStone());
        Permanent second = new Permanent(new KamiOfOldStone());
        gd.playerBattlefields.get(player1.getId()).addAll(List.of(first, second));

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
        harness.assertInGraveyard(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Sacrificing no Spirits still drains 2 life")
    void sacrificingNoSpiritsDrainsTwo() {
        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetItsController() {
        Permanent spirit = new Permanent(new KamiOfOldStone());
        gd.playerBattlefields.get(player1.getId()).add(spirit);

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, player1.getId(), List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot sacrifice a non-Spirit to pay the cost")
    void cannotSacrificeNonSpirit() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot sacrifice a Spirit an opponent controls")
    void cannotSacrificeOpponentSpirit() {
        Permanent spirit = new Permanent(new KamiOfOldStone());
        gd.playerBattlefields.get(player2.getId()).add(spirit);

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(spirit.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Kami of Old Stone");
    }
}
