package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SuqAtaFirewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's red spells cannot target Suq'Ata Firewalker")
    void opponentRedSpellsCannotTarget() {
        harness.addToBattlefield(player2, new SuqAtaFirewalker());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player2, "Suq'Ata Firewalker")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red");
    }

    @Test
    @DisplayName("The controller's own red spells cannot target Suq'Ata Firewalker either")
    void ownRedSpellsCannotTarget() {
        harness.addToBattlefield(player1, new SuqAtaFirewalker());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, harness.getPermanentId(player1, "Suq'Ata Firewalker")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red");
    }

    @Test
    @DisplayName("Nonred spells can target Suq'Ata Firewalker")
    void nonRedSpellsCanTarget() {
        harness.addToBattlefield(player2, new SuqAtaFirewalker());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Suq'Ata Firewalker"));

        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Boomerang"));
    }

    @Test
    @DisplayName("Abilities from red sources cannot target Suq'Ata Firewalker")
    void redSourceAbilitiesCannotTarget() {
        harness.addToBattlefield(player2, new SuqAtaFirewalker());
        addReadyPyromancer(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Suq'Ata Firewalker")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("red");
    }

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void tapAbilityDeals1Damage() {
        harness.setLife(player2, 20);
        Permanent firewalker = new Permanent(new SuqAtaFirewalker());
        firewalker.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(firewalker);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(firewalker.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void addReadyPyromancer(Player player) {
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(pyromancer);
    }
}
