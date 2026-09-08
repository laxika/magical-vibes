package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RequisitionRaid.class, FountainOfYouth.class, AngelicChorus.class, GrizzlyBears.class})
class RequisitionRaidTest extends BaseCardTest {

    @Test
    @DisplayName("The artifact mode destroys an artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        cast(new int[]{0}, List.of(harness.getPermanentId(player2, "Fountain of Youth")), 2);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The enchantment mode destroys an enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());

        cast(new int[]{1}, List.of(harness.getPermanentId(player2, "Angelic Chorus")), 2);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("The counter mode puts a counter on each creature target player controls")
    void putsCountersOnTargetPlayersCreatures() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        cast(new int[]{2}, List.of(player2.getId()), 2);

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Spree resolves all selected modes and charges one additional mana for each")
    void resolvesAllSelectedModes() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new AngelicChorus());

        cast(new int[]{0, 1, 2}, List.of(
                harness.getPermanentId(player2, "Fountain of Youth"),
                harness.getPermanentId(player2, "Angelic Chorus"),
                player2.getId()), 4);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("The artifact mode rejects a non-artifact target")
    void rejectsNonArtifactTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(creature.getId()), 2))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int mana) {
        harness.setHand(player1, List.of(new RequisitionRaid()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, mana - 1);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, targets, null);
        harness.passBothPriorities();
    }
}
