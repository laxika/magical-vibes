package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EverlastingTorment;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntimidatorInitiate;
import com.github.laxika.magicalvibes.cards.z.ZealousGuardian;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HordeOfBoggarts.class, IntimidatorInitiate.class, Forest.class,
        EverlastingTorment.class, ZealousGuardian.class})
class HordeOfBoggartsTest extends BaseCardTest {

    @Test
    @DisplayName("Horde of Boggarts is 1/1 when it is your only red permanent (counts itself)")
    void isOneOneWhenOnlyRedPermanent() {
        Permanent horde = addCreatureReady(player1, new HordeOfBoggarts());

        assertThat(gqs.getEffectivePower(gd, horde)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, horde)).isEqualTo(1);
    }

    @Test
    @DisplayName("Horde of Boggarts power and toughness equal red permanents you control")
    void ptEqualsControlledRedPermanents() {
        Permanent horde = addCreatureReady(player1, new HordeOfBoggarts());
        harness.addToBattlefield(player1, new IntimidatorInitiate());
        harness.addToBattlefield(player1, new IntimidatorInitiate());

        assertThat(gqs.getEffectivePower(gd, horde)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, horde)).isEqualTo(3);
    }

    @Test
    @DisplayName("Horde of Boggarts ignores non-red and opponent permanents")
    void ignoresNonRedAndOpponentPermanents() {
        Permanent horde = addCreatureReady(player1, new HordeOfBoggarts());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new IntimidatorInitiate());

        assertThat(gqs.getEffectivePower(gd, horde)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, horde)).isEqualTo(1);
    }

    @Test
    @DisplayName("Horde of Boggarts counts red noncreature permanents")
    void countsRedNoncreaturePermanents() {
        Permanent horde = addCreatureReady(player1, new HordeOfBoggarts());
        harness.addToBattlefield(player1, new EverlastingTorment());

        assertThat(gqs.getEffectivePower(gd, horde)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, horde)).isEqualTo(2);
    }

    @Test
    @DisplayName("Menace prevents Horde of Boggarts from being blocked by one creature")
    void menaceRequiresAtLeastTwoBlockers() {
        addCreatureReady(player1, new HordeOfBoggarts());
        addCreatureReady(player2, new ZealousGuardian());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Menace allows Horde of Boggarts to be blocked by two creatures")
    void menaceAllowsAtLeastTwoBlockers() {
        addCreatureReady(player1, new HordeOfBoggarts());
        Permanent firstBlocker = addCreatureReady(player2, new ZealousGuardian());
        Permanent secondBlocker = addCreatureReady(player2, new ZealousGuardian());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
    }
}
