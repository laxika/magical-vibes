package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConsumeStrength;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpectralLynx.class, ConsumeStrength.class, PhyrexianRager.class, UrborgElf.class})
class SpectralLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from green prevents green creatures from blocking Spectral Lynx")
    void protectionFromGreenPreventsBlocking() {
        addCreatureReady(player1, new SpectralLynx());
        addCreatureReady(player2, new UrborgElf());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Black creature can deal combat damage to Spectral Lynx")
    void protectionDoesNotPreventBlackCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new PhyrexianRager());
        addCreatureReady(player2, new SpectralLynx());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Spectral Lynx");
        harness.assertInGraveyard(player2, "Spectral Lynx");
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {B} grants Spectral Lynx a regeneration shield")
    void blackAbilityGrantsRegenerationShield() {
        Permanent lynx = addCreatureReady(player1, new SpectralLynx());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lynx.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Protection from green prevents a green spell from targeting Spectral Lynx")
    void protectionFromGreenPreventsTargeting() {
        Permanent lynx = addCreatureReady(player1, new SpectralLynx());
        Permanent otherCreature = addCreatureReady(player2, new PhyrexianRager());

        harness.setHand(player2, List.of(new ConsumeStrength()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                List.of(lynx.getId(), otherCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("The regeneration ability saves Spectral Lynx from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent lynx = addCreatureReady(player1, new SpectralLynx());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addCreatureReady(player2, new PhyrexianRager());
        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Spectral Lynx");
        assertThat(lynx.isTapped()).isTrue();
        assertThat(lynx.isBlocking()).isFalse();
        assertThat(lynx.getMarkedDamage()).isZero();
        assertThat(lynx.getRegenerationShield()).isZero();
    }
}
