package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RipjawRaptor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WaytaTrainerProdigy.class, RipjawRaptor.class, GrizzlyBears.class})
class WaytaTrainerProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the fight ability to {G} for two creatures you control and doubles damage triggers")
    void discountsOwnTargetsAndDoublesDamageTrigger() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent raptor = addCreatureReady(player1, new RipjawRaptor());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(wayta), 0,
                List.of(raptor.getId(), bear.getId()));

        assertThat(wayta.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(raptor.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Does not reduce the cost when the second creature is controlled by an opponent")
    void requiresBothTargetsToBeControlled() {
        Permanent wayta = addCreatureReady(player1, new WaytaTrainerProdigy());
        Permanent raptor = addCreatureReady(player1, new RipjawRaptor());
        Permanent opposingBear = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, battlefieldIndex(wayta), 0, List.of(raptor.getId(), opposingBear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wayta.isTapped()).isFalse();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
