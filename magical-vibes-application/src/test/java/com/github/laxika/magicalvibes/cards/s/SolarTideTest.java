package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SolarTideTest extends BaseCardTest {

    @Test
    @DisplayName("The low-power mode destroys creatures with power 2 or less")
    void destroysLowPowerCreatures() {
        Permanent onePower = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent twoPower = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent threePower = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(new int[]{0}, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(onePower);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(twoPower);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(threePower);
    }

    @Test
    @DisplayName("The high-power mode destroys creatures with power 3 or greater")
    void destroysHighPowerCreatures() {
        Permanent twoPower = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent threePower = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(new int[]{1}, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(twoPower);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(threePower);
    }

    @Test
    @DisplayName("Choosing both modes sacrifices two lands and destroys all creatures")
    void entwinedSacrificesTwoLandsAndResolvesBothModes() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent onePower = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent threePower = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(new int[]{0, 1}, List.of(firstLand.getId(), secondLand.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstLand, secondLand, onePower);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(threePower);
    }

    @Test
    @DisplayName("Choosing both modes requires two lands")
    void entwinedRequiresTwoLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new GrizzlyBears());

        addMana();
        harness.setHand(player1, List.of(new SolarTide()));

        assertThatThrownBy(() -> harness.castModalSorceryWithModesAndSacrifices(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
    }

    private void cast(int[] modes, List<UUID> sacrificePermanentIds) {
        addMana();
        harness.setHand(player1, List.of(new SolarTide()));
        harness.castModalSorceryWithModesAndSacrifices(
                player1, 0, 1, 2, modes, sacrificePermanentIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
