package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevouringGreed.class, HumbleBudoka.class, KamiOfOldStone.class})
class DevouringGreedTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Drains 2 life plus 2 for each Spirit sacrificed")
    void drainsTwoPlusTwoPerSacrificedSpirit() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());

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
    @DisplayName("Counts only the Spirits actually chosen for the additional cost")
    void countsOnlyChosenSpirits() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());
        Permanent remainingFirst = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());
        Permanent remainingSecond = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        harness.castSorceryWithSacrifices(player1, 0, player2.getId(), List.of(sacrificed.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .containsExactly(remainingFirst, remainingSecond);
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
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());

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
        Permanent nonSpirit = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(nonSpirit.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Humble Budoka");
    }

    @Test
    @DisplayName("Cannot sacrifice a Spirit an opponent controls")
    void cannotSacrificeOpponentSpirit() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player2, new KamiOfOldStone());

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(spirit.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Cannot choose the same Spirit twice for the additional cost")
    void cannotSacrificeSameSpiritTwice() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new KamiOfOldStone());

        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, player2.getId(),
                List.of(spirit.getId(), spirit.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Requires a target player")
    void cannotCastWithoutTargetPlayer() {
        harness.setHand(player1, List.of(new DevouringGreed()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifices(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
