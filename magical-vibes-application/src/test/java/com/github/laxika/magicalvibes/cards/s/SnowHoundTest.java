package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnowHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Ability returns Snow Hound and target green creature you control to hand")
    void returnsSelfAndGreenCreature() {
        Permanent hound = addCreatureReady(player1, new SnowHound());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(hound.getId()) || p.getId().equals(bears.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Snow Hound"))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(hound.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability returns Snow Hound and target blue creature you control to hand")
    void returnsSelfAndBlueCreature() {
        Permanent hound = addCreatureReady(player1, new SnowHound());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, wizard.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(hound.getId()) || p.getId().equals(wizard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Snow Hound"))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Cannot target a non-green non-blue creature")
    void cannotTargetWhiteCreature() {
        addCreatureReady(player1, new SnowHound());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, vanguard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's green creature")
    void cannotTargetOpponentsCreature() {
        addCreatureReady(player1, new SnowHound());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
