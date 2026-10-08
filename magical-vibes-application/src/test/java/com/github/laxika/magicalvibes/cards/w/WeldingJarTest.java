package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeldingJar.class, Ornithopter.class, FangrenHunter.class, Shatter.class})
class WeldingJarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Welding Jar regenerates target artifact")
    void sacrificesJarAndRegeneratesTargetArtifact() {
        harness.addToBattlefield(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Welding Jar");
        harness.assertInGraveyard(player1, "Welding Jar");

        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Ornithopter").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves the target artifact from destruction")
    void regenerationShieldSavesArtifactFromDestruction() {
        harness.addToBattlefield(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Welding Jar cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        harness.addToBattlefield(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FangrenHunter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
        harness.assertOnBattlefield(player1, "Welding Jar");
        harness.assertNotInGraveyard(player1, "Welding Jar");
    }

    @Test
    @DisplayName("Welding Jar can regenerate a tapped noncreature artifact")
    void regeneratesTappedNoncreatureArtifact() {
        harness.addToBattlefield(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WeldingJar());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Welding Jar");
        harness.assertNotInGraveyard(player2, "Welding Jar");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Welding Jar can target itself but sacrifice is not prevented by regeneration")
    void canTargetItselfButCannotRegenerateItsSacrifice() {
        Permanent jar = harness.addToBattlefieldAndReturn(player1, new WeldingJar());

        harness.activateAbility(player1, 0, null, jar.getId());
        harness.assertNotOnBattlefield(player1, "Welding Jar");
        harness.assertInGraveyard(player1, "Welding Jar");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Welding Jar");
        harness.assertInGraveyard(player1, "Welding Jar");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A regeneration shield waits for destruction and protects only once")
    void shieldWaitsForDestructionAndProtectsOnlyOnce() {
        harness.addToBattlefield(player1, new WeldingJar());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        target.setMarkedDamage(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.setHand(player1, List.of(new Shatter(), new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isZero();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
    }
}
