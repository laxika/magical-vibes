package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpectralShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns target Spirit you control to its owner's hand")
    void abilityReturnsControlledSpiritToHand() {
        addReadyShepherd(player1);
        Permanent spirit = addReadySpirit(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, spirit.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Apothecary Geist"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Apothecary Geist"));
    }

    @Test
    @DisplayName("Ability can bounce Spectral Shepherd itself")
    void canBounceItself() {
        Permanent shepherd = addReadyShepherd(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, shepherd.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spectral Shepherd"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spectral Shepherd"));
    }

    @Test
    @DisplayName("Cannot target a non-Spirit creature")
    void cannotTargetNonSpirit() {
        addReadyShepherd(player1);
        Permanent bears = addReadyBears(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's Spirit")
    void cannotTargetOpponentsSpirit() {
        addReadyShepherd(player1);
        Permanent spirit = addReadySpirit(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spirit.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyShepherd(Player player) {
        Permanent perm = new Permanent(new SpectralShepherd());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpirit(Player player) {
        Permanent perm = new Permanent(new ApothecaryGeist());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
