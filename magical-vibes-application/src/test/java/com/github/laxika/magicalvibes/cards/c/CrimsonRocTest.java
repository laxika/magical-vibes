package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimsonRoc.class, BayFalcon.class, FemerefScouts.class})
class CrimsonRocTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature without flying gives Crimson Roc +1/+0 and first strike")
    void blocksNonFlyerBoostsAndGrantsFirstStrike() {
        addCreatureReady(player1, new FemerefScouts());
        Permanent roc = addCreatureReady(player2, new CrimsonRoc());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isEqualTo(1);
        assertThat(roc.getToughnessModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Blocking a flying creature gives Crimson Roc nothing")
    void blocksFlyerDoesNothing() {
        addCreatureReady(player1, new BayFalcon());
        Permanent roc = addCreatureReady(player2, new CrimsonRoc());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Becoming blocked by a creature without flying does not trigger Crimson Roc")
    void becomesBlockedDoesNothing() {
        Permanent roc = addCreatureReady(player1, new CrimsonRoc());
        addCreatureReady(player2, new FemerefScouts()).getGrantedKeywords().add(Keyword.REACH);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Blocking multiple non-flying creatures triggers once for each creature")
    @CardUsed(HighGround.class)
    void blocksMultipleNonFlyersBoostsOncePerCreature() {
        harness.addToBattlefield(player2, new HighGround());
        addCreatureReady(player1, new FemerefScouts());
        addCreatureReady(player1, new FemerefScouts());
        Permanent roc = addCreatureReady(player2, new CrimsonRoc());

        declareAttackers(List.of(0, 1));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        assertThat(roc.getPowerModifier()).isEqualTo(2);
        assertThat(roc.getToughnessModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("The boost and first strike last only until end of turn")
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        addCreatureReady(player1, new FemerefScouts());
        Permanent roc = addCreatureReady(player2, new CrimsonRoc());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isEqualTo(1);
        assertThat(roc.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isZero();
        assertThat(roc.getToughnessModifier()).isZero();
        assertThat(roc.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("The trigger uses flying as it existed when the block was declared")
    void triggerUsesFlyingAtBlockTime() {
        Permanent attacker = addCreatureReady(player1, new FemerefScouts());
        Permanent roc = addCreatureReady(player2, new CrimsonRoc());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        attacker.getGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        assertThat(roc.getPowerModifier()).isEqualTo(1);
        assertThat(roc.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }
}
