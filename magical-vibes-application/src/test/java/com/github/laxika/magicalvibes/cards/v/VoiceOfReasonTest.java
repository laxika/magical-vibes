package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Disappear;
import com.github.laxika.magicalvibes.cards.f.FendOff;
import com.github.laxika.magicalvibes.cards.o.Opposition;
import com.github.laxika.magicalvibes.cards.t.ThievingMagpie;
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

@CardUsed({VoiceOfReason.class, Disappear.class, FendOff.class, Opposition.class,
        ThievingMagpie.class, VoiceOfDuty.class})
class VoiceOfReasonTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be targeted by a blue Aura")
    void cannotBeTargetedByBlueAura() {
        Permanent voice = addCreatureReady(player2, new VoiceOfReason());

        harness.setHand(player1, List.of(new Disappear()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by a non-blue spell")
    void canBeTargetedByNonBlueSpell() {
        Permanent voice = addCreatureReady(player2, new VoiceOfReason());
        FendOff fendOff = new FendOff();

        harness.setHand(player1, List.of(fendOff));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, voice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(fendOff);
    }

    @Test
    @DisplayName("Cannot be targeted by an ability of a blue source")
    void cannotBeTargetedByBlueAbility() {
        Permanent opposition = harness.addToBattlefieldAndReturn(player1, new Opposition());
        addCreatureReady(player1, new ThievingMagpie());
        Permanent voice = addCreatureReady(player2, new VoiceOfReason());

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(opposition),
                null,
                voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("A blue creature cannot block Voice of Reason")
    void blueCreatureCannotBlock() {
        addCreatureReady(player1, new VoiceOfReason());
        addCreatureReady(player2, new ThievingMagpie());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A non-blue creature can block Voice of Reason")
    void nonBlueCreatureCanBlock() {
        addCreatureReady(player1, new VoiceOfReason());
        Permanent blocker = addCreatureReady(player2, new VoiceOfDuty());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Survives combat damage from a blue creature")
    void survivesDamageFromBlueCreature() {
        addCreatureReady(player1, new ThievingMagpie());
        Permanent voice = addCreatureReady(player2, new VoiceOfReason());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Voice of Reason");
        harness.assertOnBattlefield(player1, "Thieving Magpie");
        assertThat(voice.getMarkedDamage()).isZero();
    }
}
