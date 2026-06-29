package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VedalkenCertarchTest extends BaseCardTest {

    private void addCertarchReady() {
        harness.addToBattlefield(player1, new VedalkenCertarch());
        Permanent certarch = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vedalken Certarch"))
                .findFirst().orElseThrow();
        certarch.setSummoningSick(false);
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has tap activated ability with metalcraft restriction")
    void hasCorrectAbility() {
        VedalkenCertarch card = new VedalkenCertarch();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(TapTargetPermanentEffect.class);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.METALCRAFT);
    }

    // ===== Metalcraft activation restriction =====

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

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
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

        Permanent spellbook = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spellbook"))
                .findFirst().orElseThrow();
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
}
