package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DampenThought;
import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfTheHunt.class, DampenThought.class, HumbleBudoka.class})
class KamiOfTheHuntTest extends BaseCardTest {

    private Permanent addKami() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTheHunt());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return kami;
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast a Spirit spell")
    void pumpsOnSpiritCast() {
        Permanent kami = addKami();

        harness.setHand(player1, List.of(new KamiOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 when you cast an Arcane spell")
    void pumpsOnArcaneCast() {
        Permanent kami = addKami();

        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger on a spell that is neither Spirit nor Arcane")
    void noPumpOnUnrelatedSpell() {
        Permanent kami = addKami();

        harness.setHand(player1, List.of(new HumbleBudoka()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a Spirit spell")
    void noPumpOnOpponentSpiritCast() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTheHunt());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new KamiOfTheHunt()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent kami = addKami();

        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent afterCleanup = findPermanent(player1, "Kami of the Hunt");
        assertThat(gqs.getEffectivePower(gd, afterCleanup)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterCleanup)).isEqualTo(2);
    }
}
