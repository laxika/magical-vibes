package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlexiosDeimosOfKosmos;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

@CardUsed({MercyKilling.class, GrizzlyBears.class, Ornithopter.class, FountainOfYouth.class,
        AlexiosDeimosOfKosmos.class})
class MercyKillingTest extends BaseCardTest {

    private long elfWarriorCount(Player player) {
        return countPermanents(player, "Elf Warrior");
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new MercyKilling()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Sacrifices the target and its controller creates power-many Elf Warrior tokens")
    void sacrificesAndCreatesTokensEqualToPower() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        castAt(bears);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // The target's controller (player2), not the caster, gets the tokens; X = power (2).
        assertThat(elfWarriorCount(player2)).isEqualTo(2);
        assertThat(elfWarriorCount(player1)).isZero();
    }

    @Test
    @DisplayName("A zero-power creature is sacrificed but creates no tokens")
    void zeroPowerCreatesNoTokens() {
        Permanent ornithopter = addCreatureReady(player2, new Ornithopter()); // 0/2

        castAt(ornithopter);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
        assertThat(elfWarriorCount(player2)).isZero();
    }

    @Test
    @DisplayName("Can target and sacrifice the caster's own creature")
    void canTargetOwnCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2

        castAt(bears);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(elfWarriorCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new GrizzlyBears()); // a legal creature exists, so the spell is playable
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MercyKilling()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Uses modified power immediately before sacrifice")
    void usesModifiedPower() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setPowerModifier(3);

        castAt(bears);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(elfWarriorCount(player2)).isEqualTo(5);
    }

    @Test
    @DisplayName("Negative power creates no tokens but still sacrifices the creature")
    void negativePowerCreatesNoTokens() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setPowerModifier(-3);

        castAt(bears);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(elfWarriorCount(player2)).isZero();
    }

    @Test
    @DisplayName("A target sacrificed in response does not produce tokens a second time")
    void targetLeavingBeforeResolutionProducesNoAdditionalTokens() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MercyKilling(), new MercyKilling()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castInstant(player1, 0, bears.getId());
        harness.castInstant(player1, 0, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(elfWarriorCount(player2)).isEqualTo(2);
        assertThat(elfWarriorCount(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature that cannot be sacrificed remains but its controller still creates tokens")
    void cannotSacrificeStillCreatesTokens() {
        Permanent alexios = addCreatureReady(player2, new AlexiosDeimosOfKosmos());

        castAt(alexios);

        harness.assertOnBattlefield(player2, "Alexios, Deimos of Kosmos");
        harness.assertNotInGraveyard(player2, "Alexios, Deimos of Kosmos");
        assertThat(elfWarriorCount(player2)).isEqualTo(4);
        assertThat(elfWarriorCount(player1)).isZero();
    }
}
