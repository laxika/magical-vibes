package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.t.TopanFreeblade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmprynTactician.class, TopanFreeblade.class})
class AmprynTacticianTest extends BaseCardTest {

    private void castTactician() {
        harness.setHand(player1, List.of(new AmprynTactician()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Entering boosts other creatures you control and itself")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new TopanFreeblade());

        castTactician();

        Permanent freeblade = findPermanent(player1, "Topan Freeblade");
        assertThat(freeblade.getEffectivePower()).isEqualTo(3);
        assertThat(freeblade.getEffectiveToughness()).isEqualTo(3);

        Permanent tactician = findPermanent(player1, "Ampryn Tactician");
        assertThat(tactician.getEffectivePower()).isEqualTo(4);
        assertThat(tactician.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new TopanFreeblade());

        castTactician();

        assertThat(findPermanent(player2, "Topan Freeblade").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player2, "Topan Freeblade").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new TopanFreeblade());

        castTactician();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Topan Freeblade").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player1, "Ampryn Tactician").getEffectivePower()).isEqualTo(3);
        assertThat(findPermanent(player1, "Ampryn Tactician").getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive the boost")
    void boostsCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new AmprynTactician()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent tactician = findPermanent(player1, "Ampryn Tactician");
        assertThat(tactician.getEffectivePower()).isEqualTo(3);
        assertThat(tactician.getEffectiveToughness()).isEqualTo(3);
        Permanent freeblade = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());

        resolveAllTriggers();

        assertThat(freeblade.getEffectivePower()).isEqualTo(3);
        assertThat(freeblade.getEffectiveToughness()).isEqualTo(3);
        assertThat(tactician.getEffectivePower()).isEqualTo(4);
        assertThat(tactician.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not receive the boost")
    void doesNotBoostLaterCreatures() {
        castTactician();

        Permanent freeblade = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());

        assertThat(freeblade.getEffectivePower()).isEqualTo(2);
        assertThat(freeblade.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanent(player1, "Ampryn Tactician").getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Multiple entry triggers stack their boosts on existing creatures")
    void multipleBoostsAccumulate() {
        Permanent freeblade = harness.addToBattlefieldAndReturn(player1, new TopanFreeblade());
        castTactician();
        Permanent first = findPermanent(player1, "Ampryn Tactician");

        castTactician();

        List<Permanent> tacticians = findPermanents(player1, "Ampryn Tactician");
        assertThat(tacticians).hasSize(2);
        assertThat(freeblade.getEffectivePower()).isEqualTo(4);
        assertThat(freeblade.getEffectiveToughness()).isEqualTo(4);
        assertThat(first.getEffectivePower()).isEqualTo(5);
        assertThat(first.getEffectiveToughness()).isEqualTo(5);
        assertThat(tacticians.get(1).getEffectivePower()).isEqualTo(4);
        assertThat(tacticians.get(1).getEffectiveToughness()).isEqualTo(4);
    }
}
