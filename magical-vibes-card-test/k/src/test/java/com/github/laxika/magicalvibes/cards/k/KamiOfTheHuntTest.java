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

        harness.castFromHand(player1, new KamiOfTheHunt(), "{2}{G}");

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

        harness.castFromHand(player1, new HumbleBudoka(), "{1}{G}");

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

        harness.castFromHand(player2, new KamiOfTheHunt(), "{2}{G}");
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

    @Test
    @DisplayName("Successive matching casts give cumulative boosts to the existing Kami only")
    void boostsAccumulateAndNewKamiDoesNotTriggerForItsOwnCast() {
        Permanent kami = addKami();

        harness.castFromHand(player1, new KamiOfTheHunt(), "{2}{G}");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent newKami = findPermanents(player1, "Kami of the Hunt").get(1);
        assertThat(gqs.getEffectivePower(gd, newKami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, newKami)).isEqualTo(2);

        harness.setHand(player1, List.of(new DampenThought()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, newKami)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, newKami)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Spirit entering without being cast does not trigger the ability")
    void noPumpOnSpiritEnteringWithoutCast() {
        Permanent kami = addKami();

        harness.enterBattlefieldAndReturn(player1, new KamiOfTheHunt());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts an Arcane spell")
    void noPumpOnOpponentArcaneCast() {
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfTheHunt());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new DampenThought()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }
}
