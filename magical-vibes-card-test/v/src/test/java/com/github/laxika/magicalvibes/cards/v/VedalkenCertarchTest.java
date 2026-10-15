package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FurnaceCelebration;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VedalkenCertarch.class, BottleGnomes.class, GrizzlyBears.class, LeoninScimitar.class,
        Spellbook.class, Memnite.class, Island.class, FurnaceCelebration.class})
class VedalkenCertarchTest extends BaseCardTest {

    private void addCertarchReady() {
        addCreatureReady(player1, new VedalkenCertarch());
    }

    @Test
    @DisplayName("Cannot activate without three artifacts")
    void cannotActivateWithoutThreeArtifacts() {
        addCertarchReady();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
    }

    @Test
    @DisplayName("Can activate with three artifacts — taps target creature")
    void tapsTargetCreatureWithMetalcraft() {
        addCertarchReady();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap target artifact")
    void tapsTargetArtifact() {
        addCertarchReady();
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new Spellbook());

        UUID targetId = harness.getPermanentId(player2, "Spellbook");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent spellbook = findPermanent(player2, "Spellbook");
        assertThat(spellbook.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Has summoning sickness — cannot activate on first turn")
    void respectsSummoningSickness() {
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Add Certarch without clearing summoning sickness
        harness.addToBattlefield(player1, new VedalkenCertarch());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSomMetalcraft() {
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
    }

    @Test
    @DisplayName("Taps a land and pays the tap cost immediately without mana")
    void tapsTargetLand() {
        addCertarchReady();
        addSomMetalcraft();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(findPermanent(player1, "Vedalken Certarch").isTapped()).isTrue();
        assertThat(land.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent artifacts do not satisfy metalcraft")
    void opponentArtifactsDoNotCount() {
        addCertarchReady();
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Memnite());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Metalcraft");
        assertThat(findPermanent(player1, "Vedalken Certarch").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target an enchantment that is not an artifact, creature, or land")
    void rejectsEnchantmentTarget() {
        addCertarchReady();
        addSomMetalcraft();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceCelebration());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Vedalken Certarch").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can target an already tapped artifact controlled by its controller")
    void canTargetOwnTappedArtifact() {
        addCertarchReady();
        addSomMetalcraft();
        Permanent target = findPermanent(player1, "Memnite");
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Vedalken Certarch").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing metalcraft after activation does not stop the ability")
    void resolvesAfterLosingMetalcraft() {
        addCertarchReady();
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new BottleGnomes());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 3, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Bottle Gnomes")).isZero();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Certarch cannot activate again")
    void cannotActivateTwiceWithoutUntapping() {
        addCertarchReady();
        addSomMetalcraft();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
    }
}
