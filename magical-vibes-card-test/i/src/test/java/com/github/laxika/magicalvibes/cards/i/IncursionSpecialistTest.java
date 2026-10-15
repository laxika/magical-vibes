package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IncursionSpecialist.class, LightningBolt.class})
class IncursionSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell boosts Incursion Specialist and makes it unblockable until end of turn")
    void secondSpellBoostsAndMakesUnblockableUntilEndOfTurn() {
        Permanent specialist = addCreatureReady(player1, new IncursionSpecialist());
        int initialPower = specialist.getEffectivePower();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower);
        assertThat(specialist.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(specialist.isCantBeBlocked()).isTrue();
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(specialist.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower);
        assertThat(specialist.isCantBeBlocked()).isFalse();
    }


    @Test
    void creatureSpellsCountIncludingSpecialistCastBeforeItEntered() {
        harness.setHand(player1, List.of(new IncursionSpecialist(), new IncursionSpecialist()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent first = findPermanent(player1, "Incursion Specialist");
        int initialPower = first.getEffectivePower();
        int initialToughness = first.getEffectiveToughness();
        assertThat(first.isCantBeBlocked()).isFalse();

        harness.castCreature(player1, 0);
        assertThat(first.getEffectivePower()).isEqualTo(initialPower);
        assertThat(first.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();
        assertThat(first.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(first.getEffectiveToughness()).isEqualTo(initialToughness);
        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        Permanent second = findPermanents(player1, "Incursion Specialist").stream()
                .filter(permanent -> !permanent.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        assertThat(second.getEffectivePower()).isEqualTo(initialPower);
        assertThat(second.isCantBeBlocked()).isFalse();
    }

    @Test
    void opponentsSpellsDoNotCountAndAbilityWorksOnOpponentsTurn() {
        Permanent specialist = addCreatureReady(player1, new IncursionSpecialist());
        int initialPower = specialist.getEffectivePower();
        gd.activePlayerId = player2.getId();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower);
        assertThat(specialist.isCantBeBlocked()).isFalse();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower);
        assertThat(specialist.isCantBeBlocked()).isFalse();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(specialist.isCantBeBlocked()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    void spellCountResetsOnTheNextTurn() {
        Permanent specialist = addCreatureReady(player1, new IncursionSpecialist());
        int initialPower = specialist.getEffectivePower();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower);
        assertThat(specialist.isCantBeBlocked()).isFalse();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(specialist.getEffectivePower()).isEqualTo(initialPower + 2);
        assertThat(specialist.isCantBeBlocked()).isTrue();
        harness.passBothPriorities();
    }

}
