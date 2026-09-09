package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreasureTrove.class, RagingGoblin.class})
class TreasureTroveTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability with mana draws a card")
    void activatingDrawsACard() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of(new RagingGoblin()));
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()).get(1).getName()).isEqualTo("Raging Goblin");
    }

    @Test
    @DisplayName("Ability can be activated repeatedly since it does not tap")
    void canActivateRepeatedly() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RagingGoblin(), new RagingGoblin()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Requires two blue mana in addition to the generic cost")
    void requiresTwoBlueMana() {
        Permanent trove = addTrove(player1);
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, trove), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay the generic cost with colorless mana")
    void paysGenericCostWithColorlessMana() {
        Permanent trove = addTrove(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RagingGoblin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, indexOf(player1, trove), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent trove = addTrove(player1);
        harness.setLibrary(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, trove), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTrove(Player player) {
        Permanent perm = new Permanent(new TreasureTrove());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
