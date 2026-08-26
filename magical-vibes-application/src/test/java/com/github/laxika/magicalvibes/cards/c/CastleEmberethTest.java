package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CastleEmbereth.class, Mountain.class, GrizzlyBears.class})
class CastleEmberethTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without a Mountain")
    void entersTappedWithoutMountain() {
        harness.setHand(player1, List.of(new CastleEmbereth()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Embereth").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new CastleEmbereth()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Castle Embereth").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana ability adds red mana")
    void manaAbilityAddsRed() {
        harness.addToBattlefield(player1, new CastleEmbereth());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = findPermanent(player1, "Castle Embereth");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Pump ability boosts only your creatures until end of turn")
    void pumpAbilityBoostsOwnCreaturesUntilEndOfTurn() {
        harness.addToBattlefield(player1, new CastleEmbereth());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }
}
