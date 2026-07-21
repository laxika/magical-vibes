package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FiligreeSagesTest extends BaseCardTest {

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Untaps a tapped artifact")
    void untapsTappedArtifact() {
        harness.addToBattlefield(player1, new FiligreeSages());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        target.tap();
        addAbilityMana();

        assertThat(target.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can untap own tapped artifact")
    void canUntapOwnArtifact() {
        harness.addToBattlefield(player1, new FiligreeSages());
        harness.addToBattlefield(player1, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player1, "Angel's Feather");
        Permanent target = gd.playerBattlefields.get(player1.getId()).get(1);
        target.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new FiligreeSages());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new FiligreeSages());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID targetId = harness.getPermanentId(player2, "Angel's Feather");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can untap itself as an artifact creature")
    void canUntapSelf() {
        Permanent sages = harness.addToBattlefieldAndReturn(player1, new FiligreeSages());
        sages.tap();
        addAbilityMana();

        harness.activateAbility(player1, 0, null, sages.getId());
        harness.passBothPriorities();

        assertThat(sages.isTapped()).isFalse();
    }
}
