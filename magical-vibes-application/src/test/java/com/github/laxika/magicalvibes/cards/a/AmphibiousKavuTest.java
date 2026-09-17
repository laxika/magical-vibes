package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.NightscapeFamiliar;
import com.github.laxika.magicalvibes.cards.s.SeaSnidd;
import com.github.laxika.magicalvibes.cards.t.ThornscapeFamiliar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AmphibiousKavu.class, SeaSnidd.class, NightscapeFamiliar.class, ThornscapeFamiliar.class})
class AmphibiousKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Amphibious Kavu gets +3/+3 when blocked by a blue creature")
    void becomesBlockedByBlueCreatureBoosts() {
        Permanent kavu = addKavu(player1);
        addCreatureReady(player2, new SeaSnidd());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Amphibious Kavu gets +3/+3 when blocking a black creature")
    void blocksBlackCreatureBoosts() {
        addCreatureReady(player1, new NightscapeFamiliar());
        Permanent kavu = addKavu(player2);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Amphibious Kavu does not get a boost from a creature of another color")
    void doesNotBoostForOtherColor() {
        Permanent kavu = addKavu(player1);
        addCreatureReady(player2, new ThornscapeFamiliar());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isZero();
        assertThat(kavu.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Amphibious Kavu gets only one boost when blocking multiple matching creatures")
    void multipleMatchingBlockersBoostOnce() {
        Permanent kavu = addKavu(player1);
        addCreatureReady(player2, new SeaSnidd());
        addCreatureReady(player2, new NightscapeFamiliar());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Amphibious Kavu gets only one boost when blocking multiple matching creatures")
    void multipleMatchingAttackersBoostOnce() {
        AmphibiousKavu card = new AmphibiousKavu();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent kavu = addCreatureReady(player2, card);
        Permanent seaSnidd = addCreatureReady(player1, new SeaSnidd());
        Permanent nightscapeFamiliar = addCreatureReady(player1, new NightscapeFamiliar());

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);

        harness.handleCombatDamageAssigned(player2, 0, Map.of(
                seaSnidd.getId(), 3,
                nightscapeFamiliar.getId(), 2));
    }

    @Test
    @DisplayName("Amphibious Kavu's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent kavu = addKavu(player1);
        addCreatureReady(player2, new SeaSnidd());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isEqualTo(3);
        assertThat(kavu.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kavu.getPowerModifier()).isZero();
        assertThat(kavu.getToughnessModifier()).isZero();
    }

    private Permanent addKavu(Player player) {
        return addCreatureReady(player, new AmphibiousKavu());
    }
}
