package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.m.MagnigothSentry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathbloomGardener.class, ColossalDreadmaw.class, MagnigothSentry.class})
class DeathbloomGardenerTest extends BaseCardTest {

    @Test
    @DisplayName("Deathbloom Gardener adds one mana of the chosen color")
    void addsChosenColorMana() {
        addCreatureReady(player1, new DeathbloomGardener());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger blocker in combat")
    void deathtouchDestroysLargerBlocker() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new DeathbloomGardener());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        gardener.setSummoningSick(false);
        gardener.setAttacking(true);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(gardener.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Mana ability produces exactly one mana immediately in each available color")
    void producesEachColorWithoutUsingStack(ManaColor color) {
        Permanent gardener = addCreatureReady(player1, new DeathbloomGardener());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gardener.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
    }

    @Test
    @DisplayName("Summoning sickness prevents activation of the tap ability")
    void summoningSicknessPreventsManaAbility() {
        Permanent gardener = harness.addToBattlefieldAndReturn(player1, new DeathbloomGardener());
        gardener.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gardener.isTapped()).isFalse();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @Test
    @DisplayName("The tapped Gardener cannot activate again without untapping")
    void cannotActivateTwiceWhileTapped() {
        Permanent gardener = addCreatureReady(player1, new DeathbloomGardener());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gardener.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch also destroys a larger attacker when Gardener blocks")
    void deathtouchDestroysLargerAttacker() {
        Permanent attacker = addCreatureReady(player1, new MagnigothSentry());
        Permanent gardener = harness.addToBattlefieldAndReturn(player2, new DeathbloomGardener());
        attacker.setAttacking(true);
        gardener.setBlocking(true);
        gardener.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Magnigoth Sentry");
        harness.assertInGraveyard(player2, "Deathbloom Gardener");
        harness.assertNotOnBattlefield(player1, "Magnigoth Sentry");
        harness.assertNotOnBattlefield(player2, "Deathbloom Gardener");
    }
}
