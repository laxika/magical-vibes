package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThousandYearElixirTest extends BaseCardTest {

    // ===== {1}, {T}: Untap target creature =====

    @Test
    @DisplayName("Untaps a tapped target creature")
    void untapsTargetCreature() {
        harness.addToBattlefield(player1, new ThousandYearElixir());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        Permanent target = gd.playerBattlefields.get(player2.getId()).get(0);
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new ThousandYearElixir());
        harness.addToBattlefield(player2, new AngelsFeather());
        UUID artifactId = harness.getPermanentId(player2, "Angel's Feather");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Static: activate abilities as though creatures had haste =====

    @Test
    @DisplayName("A summoning-sick creature you control may use its tap ability")
    void summoningSickCreatureCanTapForAbility() {
        harness.addToBattlefield(player1, new ThousandYearElixir());
        harness.addToBattlefield(player1, new LlanowarElves()); // summoning sick by default
        Permanent elves = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(elves.isSummoningSick()).isTrue();

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without the Elixir a summoning-sick creature cannot use its tap ability")
    void withoutElixirSummoningSickCreatureCannotTap() {
        harness.addToBattlefield(player1, new LlanowarElves()); // summoning sick by default

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("The Elixir only helps its own controller's creatures")
    void doesNotHelpOpponentsCreatures() {
        harness.addToBattlefield(player1, new ThousandYearElixir());
        harness.addToBattlefield(player2, new LlanowarElves()); // opponent's, summoning sick

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }
}
