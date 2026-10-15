package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KruinOutlaw.class, GatstafShepherd.class, WalkingCorpse.class})
class KruinOutlawTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms to Terror of Kruin Pass when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(outlaw.isTransformed()).isFalse();
        assertThat(outlaw.getCard().getName()).isEqualTo("Kruin Outlaw");
    }

    @Test
    @DisplayName("Terror of Kruin Pass transforms back when a player cast two or more spells last turn")
    void terrorTransformsBackWhenTwoSpellsCast() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        // Transform to Terror of Kruin Pass first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Now simulate that a player cast 2+ spells last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve transform back

        assertThat(outlaw.isTransformed()).isFalse();
        assertThat(outlaw.getCard().getName()).isEqualTo("Kruin Outlaw");
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not transform back when only one spell was cast last turn")
    void terrorDoesNotTransformWhenOneSpellCast() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        // Transform to Terror of Kruin Pass first
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Only 1 spell cast last turn
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player2);

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
    }

    @Test
    @DisplayName("Transform triggers on opponent's upkeep too")
    void transformTriggersOnOpponentUpkeep() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        gd.spellsCastLastTurn.clear();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve

        assertThat(outlaw.isTransformed()).isTrue();
        assertThat(outlaw.getCard().getName()).isEqualTo("Terror of Kruin Pass");
    }

    @Test
    @DisplayName("Terror of Kruin Pass grants menace to other werewolves you control")
    void backFaceGrantsMenaceToOtherWerewolves() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add another werewolf
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Terror of Kruin Pass itself has menace")
    void backFaceHasMenaceItself() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());
        outlaw.setSummoningSick(false);

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not grant menace to non-werewolf creatures")
    void backFaceDoesNotGrantMenaceToNonWerewolves() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add a non-werewolf creature
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        assertThat(gqs.hasKeyword(gd, corpse, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Terror of Kruin Pass does not grant menace to opponent's werewolves")
    void backFaceDoesNotGrantMenaceToOpponentWerewolves() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());

        // Transform to Terror of Kruin Pass
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();

        // Add a werewolf to opponent's battlefield
        Permanent oppShepherd = addCreatureReady(player2, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, oppShepherd, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Front face Kruin Outlaw does not grant menace to werewolves")
    void frontFaceDoesNotGrantMenace() {
        harness.addToBattlefield(player1, new KruinOutlaw());
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());

        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("A spell cast by the opponent prevents the front-face trigger")
    void opponentSpellPreventsTransformation() {
        Permanent outlaw = harness.addToBattlefieldAndReturn(player1, new KruinOutlaw());
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(outlaw.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Terror stays transformed after a turn with no spells")
    void backFaceStaysTransformedWithNoSpells() {
        Permanent outlaw = transformOutlaw();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(outlaw.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforming back removes the menace grant from all controlled werewolves")
    void transformingBackRemovesMenaceGrant() {
        Permanent outlaw = transformOutlaw();
        Permanent shepherd = addCreatureReady(player1, new GatstafShepherd());
        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isTrue();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(outlaw.isTransformed()).isFalse();
        assertThat(gqs.hasKeyword(gd, outlaw, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, shepherd, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Kruin Outlaw kills a blocker with first strike before it can retaliate")
    void firstStrikePreventsBlockerRetaliation() {
        addCreatureReady(player1, new KruinOutlaw());
        addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Kruin Outlaw");
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Unblocked Terror deals damage in both combat damage steps")
    void doubleStrikeDealsDamageTwice() {
        transformOutlaw();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Terror cannot be blocked by a single creature")
    void menaceRejectsSingleBlocker() {
        transformOutlaw();
        addCreatureReady(player2, new WalkingCorpse());
        addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    @DisplayName("Terror can be blocked by two creatures")
    void menaceAllowsTwoBlockers() {
        transformOutlaw();
        Permanent first = addCreatureReady(player2, new WalkingCorpse());
        Permanent second = addCreatureReady(player2, new WalkingCorpse());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private Permanent transformOutlaw() {
        Permanent outlaw = addCreatureReady(player1, new KruinOutlaw());
        gd.spellsCastLastTurn.clear();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(outlaw.isTransformed()).isTrue();
        return outlaw;
    }
}
