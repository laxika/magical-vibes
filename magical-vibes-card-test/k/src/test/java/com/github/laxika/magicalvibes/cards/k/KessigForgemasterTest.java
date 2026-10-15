package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KessigForgemaster.class, GiantSpider.class})
class KessigForgemasterTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when no spells were cast last turn")
    void transformsWhenNoSpellsWereCastLastTurn() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolve(player1);

        assertThat(forgemaster.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenASpellWasCastLastTurn() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(forgemaster.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoOrMoreSpellsWereCastLastTurn() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(forgemaster.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        advanceToUpkeepAndResolve(player2);

        assertThat(forgemaster.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Deals damage to the creature it blocks")
    void dealsDamageWhenBlocking() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addForgemaster(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals damage to each creature blocking it")
    void dealsDamageWhenBecomingBlocked() {
        Permanent forgemaster = addForgemaster(player1);
        forgemaster.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The transformed face deals two damage to the creature it blocks")
    void transformedFaceDealsTwoDamageWhenBlocking() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(forgemaster.isTransformed()).isTrue();

        Permanent attacker = addCreatureReady(player2, new GiantSpider());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("One spell from each player does not transform the reverse face")
    void oneSpellFromEachPlayerDoesNotTransformBack() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(forgemaster.isTransformed()).isTrue();

        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        advanceToUpkeepAndResolve(player2);

        assertThat(forgemaster.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("The reverse face stays transformed after a turn with no spells")
    void reverseFaceStaysTransformedWithoutSpells() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(forgemaster.isTransformed()).isTrue();

        advanceToUpkeepAndResolve(player2);

        assertThat(forgemaster.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Each blocker receives a separate damage trigger")
    void dealsDamageToMultipleBlockers() {
        Permanent forgemaster = addForgemaster(player1);
        forgemaster.setAttacking(true);
        Permanent first = addCreatureReady(player2, new GiantSpider());
        Permanent second = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The reverse face deals two damage to each blocker")
    void transformedFaceDealsDamageToMultipleBlockers() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepAndResolve(player1);
        assertThat(forgemaster.isTransformed()).isTrue();
        forgemaster.setAttacking(true);
        Permanent first = addCreatureReady(player2, new GiantSpider());
        Permanent second = addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(first.getMarkedDamage()).isEqualTo(2);
        assertThat(second.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Transforms at the opponent's upkeep when no spells were cast")
    void transformsAtOpponentsUpkeep() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepAndResolve(player2);

        assertThat(forgemaster.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("An opponent's spell prevents the front face from transforming")
    void opponentsSpellPreventsTransformation() {
        Permanent forgemaster = addForgemaster(player1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeepAndResolve(player1);

        assertThat(forgemaster.isTransformed()).isFalse();
    }

    private Permanent addForgemaster(Player player) {
        return addCreatureReady(player, new KessigForgemaster());
    }

    private void advanceToUpkeepAndResolve(Player activePlayer) {
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();
    }
}
