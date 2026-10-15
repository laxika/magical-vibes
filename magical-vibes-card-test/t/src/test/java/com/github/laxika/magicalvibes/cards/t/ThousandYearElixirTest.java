package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowHarrier;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThousandYearElixir.class, GrizzlyBears.class, LlanowarElves.class,
        AngelsFeather.class, GoldmeadowHarrier.class})
class ThousandYearElixirTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps a tapped target creature")
    void untapsTargetCreature() {
        harness.addToBattlefield(player1, new ThousandYearElixir());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = target.getId();
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

        // The effect's PermanentIsCreaturePredicate (carried on its TargetSpec, and exposed via
        // targetPredicate() for trigger-target collection) is enforced by the declarative spec
        // interpreter, which phrases its rejection from the predicate itself.
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A summoning-sick creature you control may use its tap ability")
    void summoningSickCreatureCanTapForAbility() {
        harness.addToBattlefield(player1, new ThousandYearElixir());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
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

    @Test
    void summoningSickCreatureCanActivateNonManaAbilityWhileElixirIsTapped() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ThousandYearElixir());
        elixir.tap();
        Permanent harrier = harness.addToBattlefieldAndReturn(player1, new GoldmeadowHarrier());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldmeadowHarrier());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, null, target.getId());
        assertThat(harrier.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void elixirDoesNotAllowSummoningSickCreatureToAttack() {
        harness.addToBattlefield(player1, new ThousandYearElixir());
        harness.addToBattlefield(player1, new GoldmeadowHarrier());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Goldmeadow Harrier").isAttacking()).isFalse();
    }

    @Test
    void untappingCreatureAllowsSecondActivationInSameTurn() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ThousandYearElixir());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.tapPermanent(player1, 1);

        harness.activateAbility(player1, 0, null, elves.getId());
        assertThat(elixir.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(elves.isTapped()).isFalse();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    void canTargetUntappedCreature() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ThousandYearElixir());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldmeadowHarrier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(elixir.isTapped()).isTrue();
    }

    @Test
    void cannotActivateUntapAbilityWithoutMana() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ThousandYearElixir());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldmeadowHarrier());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elixir.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateUntapAbilityWhenElixirIsTapped() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ThousandYearElixir());
        elixir.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldmeadowHarrier());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void hastePermissionEndsWhenElixirLeavesBattlefield() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ThousandYearElixir());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.tapPermanent(player1, 1);
        elves.untap();
        gd.playerBattlefields.get(player1.getId()).remove(elixir);
        gd.playerGraveyards.get(player1.getId()).add(elixir.getCard());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(elves.isTapped()).isFalse();
    }

    @Test
    void activatedUntapAbilityResolvesAfterElixirLeavesBattlefield() {
        Permanent elixir = harness.addToBattlefieldAndReturn(player1, new ThousandYearElixir());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldmeadowHarrier());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(elixir);
        gd.playerGraveyards.get(player1.getId()).add(elixir.getCard());

        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }
}
