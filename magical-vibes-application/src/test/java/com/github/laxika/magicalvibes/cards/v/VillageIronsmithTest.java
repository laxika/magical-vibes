package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VillageIronsmith.class, WalkingCorpse.class})
class VillageIronsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Ironfang when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(ironsmith.isTransformed()).isTrue();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Ironfang");
        assertThat(gqs.getEffectivePower(gd, ironsmith)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ironsmith)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(ironsmith.isTransformed()).isFalse();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Village Ironsmith");
    }

    @Test
    @DisplayName("Ironfang transforms back when a player cast two or more spells last turn")
    void werewolfTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        // Transform to Ironfang first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(ironsmith.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(ironsmith.isTransformed()).isFalse();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Village Ironsmith");
        assertThat(gqs.getEffectivePower(gd, ironsmith)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ironsmith)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ironfang does not transform back when only one spell was cast last turn")
    void werewolfDoesNotTransformWhenOneSpellCast() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        // Transform to Ironfang first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(ironsmith.isTransformed()).isTrue();

        // Only 1 spell cast last turn by each player
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(ironsmith.isTransformed()).isTrue();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Ironfang");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(ironsmith.isTransformed()).isTrue();
        assertThat(ironsmith.getCard().getName()).isEqualTo("Ironfang");
    }

    @Test
    @DisplayName("An opponent's spell prevents transformation to Ironfang")
    void opponentSpellPreventsTransformation() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(ironsmith.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Ironfang stays transformed after a turn with no spells")
    void ironfangStaysTransformedWhenNoSpellsCast() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(ironsmith.isTransformed()).isTrue();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(ironsmith.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Ironfang transforms back on its controller's upkeep after three spells")
    void controllerSpellsTransformIronfangBack() {
        harness.addToBattlefield(player1, new VillageIronsmith());
        Permanent ironsmith = findPermanent(player1, "Village Ironsmith");
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(ironsmith.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 3);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(ironsmith.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Ironfang kills a blocker with first strike before it can deal damage")
    void ironfangDealsFirstStrikeDamage() {
        Permanent ironsmith = addCreatureReady(player1, new VillageIronsmith());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(ironsmith.isTransformed()).isTrue();
        addCreatureReady(player2, new WalkingCorpse());

        ironsmith.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Ironfang");
        harness.assertInGraveyard(player2, "Walking Corpse");
        assertThat(ironsmith.getMarkedDamage()).isZero();
    }
}
