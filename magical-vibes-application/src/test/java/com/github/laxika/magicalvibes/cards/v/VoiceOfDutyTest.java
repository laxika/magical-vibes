package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.ElvishLookout;
import com.github.laxika.magicalvibes.cards.f.FendOff;
import com.github.laxika.magicalvibes.cards.p.PatternOfRebirth;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.s.ScentOfIvy;
import com.github.laxika.magicalvibes.cards.s.SerraAdvocate;
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

@CardUsed({VoiceOfDuty.class, FendOff.class, PatternOfRebirth.class, ScentOfIvy.class, ElvishLookout.class,
        SerraAdvocate.class, PlatedSpider.class})
class VoiceOfDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be targeted by a green spell")
    void cannotBeTargetedByGreenSpell() {
        Permanent voice = addCreatureReady(player2, new VoiceOfDuty());

        harness.setHand(player1, List.of(new ScentOfIvy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Can be targeted by a non-green spell")
    void canBeTargetedByNonGreenSpell() {
        Permanent voice = addCreatureReady(player2, new VoiceOfDuty());

        harness.setHand(player1, List.of(new FendOff()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, voice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fend Off");
    }

    @Test
    @DisplayName("Cannot be enchanted by a green Aura")
    void cannotBeEnchantedByGreenAura() {
        Permanent voice = addCreatureReady(player2, new VoiceOfDuty());

        harness.setHand(player1, List.of(new PatternOfRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Green creature cannot block Voice of Duty")
    void greenCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new VoiceOfDuty());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new PlatedSpider());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-green creature can block Voice of Duty")
    void nonGreenCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new VoiceOfDuty());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAdvocate());

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Survives combat damage from a green creature")
    void survivesDamageFromGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new ElvishLookout());
        attacker.setAttacking(true);

        Permanent voice = addCreatureReady(player2, new VoiceOfDuty());
        voice.setBlocking(true);
        voice.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player2, "Voice of Duty");
        harness.assertInGraveyard(player1, "Elvish Lookout");
    }

    @Test
    @DisplayName("Takes combat damage from a non-green creature")
    void takesDamageFromNonGreenCreature() {
        Permanent attacker = addCreatureReady(player1, new SerraAdvocate());
        attacker.setAttacking(true);

        Permanent voice = addCreatureReady(player2, new VoiceOfDuty());
        voice.setBlocking(true);
        voice.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Serra Advocate");
        harness.assertInGraveyard(player2, "Voice of Duty");
    }
}
