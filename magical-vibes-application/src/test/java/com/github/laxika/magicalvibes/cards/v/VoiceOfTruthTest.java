package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlindingAngel;
import com.github.laxika.magicalvibes.cards.d.DefiantFalcon;
import com.github.laxika.magicalvibes.cards.l.Lashknife;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.t.Topple;
import com.github.laxika.magicalvibes.cards.v.ViciousHunger;
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

@CardUsed({
        VoiceOfTruth.class,
        Mossdog.class,
        DefiantFalcon.class,
        BlindingAngel.class,
        Topple.class,
        Lashknife.class,
        ViciousHunger.class
})
class VoiceOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("A nonflying creature cannot block Voice of Truth")
    void nonflyingCreatureCannotBlock() {
        addCreatureReady(player1, new VoiceOfTruth());
        addCreatureReady(player2, new Mossdog());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("White creature cannot block Voice of Truth")
    void whiteCreatureCannotBlock() {
        addCreatureReady(player1, new VoiceOfTruth());
        addCreatureReady(player2, new DefiantFalcon());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from white prevents white combat damage")
    void preventsWhiteCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new BlindingAngel());
        Permanent voice = addCreatureReady(player2, new VoiceOfTruth());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(voice.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Voice of Truth");
    }

    @Test
    @DisplayName("Voice of Truth cannot be targeted by the white spell Topple")
    void cannotBeTargetedByWhiteSpell() {
        Permanent voice = addCreatureReady(player2, new VoiceOfTruth());
        harness.setHand(player1, List.of(new Topple()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Voice of Truth cannot be enchanted by the white Aura Lashknife")
    void cannotBeEnchantedByWhiteAura() {
        Permanent voice = addCreatureReady(player2, new VoiceOfTruth());
        harness.setHand(player1, List.of(new Lashknife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Voice of Truth can be targeted by a black spell")
    void canBeTargetedByBlackSpell() {
        Permanent voice = addCreatureReady(player2, new VoiceOfTruth());
        harness.setHand(player1, List.of(new ViciousHunger()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0, voice.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
