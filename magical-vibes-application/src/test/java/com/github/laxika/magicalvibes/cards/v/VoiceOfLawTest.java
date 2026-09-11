package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArcLightning;
import com.github.laxika.magicalvibes.cards.b.Bravado;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoiceOfLaw.class, ArcLightning.class, Bravado.class, ShivanHellkite.class})
class VoiceOfLawTest extends BaseCardTest {

    @Test
    @DisplayName("A red spell cannot target Voice of Law")
    void redSpellCannotTargetVoiceOfLaw() {
        Permanent voice = addCreatureReady(player2, new VoiceOfLaw());

        harness.setHand(player1, List.of(new ArcLightning()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, Map.of(voice.getId(), 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("An ability from a red source cannot target Voice of Law")
    void redAbilityCannotTargetVoiceOfLaw() {
        Permanent voice = addCreatureReady(player2, new VoiceOfLaw());
        addCreatureReady(player1, new ShivanHellkite());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("A red Aura cannot enchant Voice of Law")
    void redAuraCannotEnchantVoiceOfLaw() {
        Permanent voice = addCreatureReady(player2, new VoiceOfLaw());

        harness.setHand(player1, List.of(new Bravado()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("A red creature cannot block Voice of Law")
    void redCreatureCannotBlockVoiceOfLaw() {
        addCreatureReady(player1, new VoiceOfLaw());
        addCreatureReady(player2, new ShivanHellkite());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage to Voice of Law")
    void preventsCombatDamageFromRedCreature() {
        addCreatureReady(player1, new ShivanHellkite());
        Permanent voice = addCreatureReady(player2, new VoiceOfLaw());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(voice.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Voice of Law");
    }
}
