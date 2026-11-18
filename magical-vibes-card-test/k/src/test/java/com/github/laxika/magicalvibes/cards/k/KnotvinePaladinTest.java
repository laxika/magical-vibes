package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.f.FieldmistBorderpost;
import com.github.laxika.magicalvibes.cards.g.GrizzledLeotau;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnotvinePaladin.class, GrizzledLeotau.class, FieldmistBorderpost.class})
class KnotvinePaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts the attack trigger on the stack")
    void attackPutsTriggerOnStack() {
        addCreatureReady(player1, new KnotvinePaladin());

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Knotvine Paladin"));
    }

    @Test
    @DisplayName("Gets +0/+0 when attacking with no other creatures (it taps itself)")
    void noBoostWhenAttackingAlone() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(0);
    }

    @Test
    @DisplayName("Gets +N/+N for each untapped creature staying back")
    void boostForUntappedCreatures() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player1, new GrizzledLeotau());
        addCreatureReady(player1, new GrizzledLeotau());

        // Only the Paladin attacks; the other two stay back untapped.
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fellow attackers are tapped and do not count; only untapped creatures do")
    void tappedAttackersDoNotCount() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player1, new GrizzledLeotau());
        addCreatureReady(player1, new GrizzledLeotau());

        // Paladin (0) and Fellow Attacker (1) attack and tap; Stay Home (2) is untapped.
        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's untapped creatures do not count")
    void opponentCreaturesDoNotCount() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player2, new GrizzledLeotau());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(0);
    }

    @Test
    void countsUntappedCreaturesAtResolutionAndKeepsThatBoost() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        Permanent first = addCreatureReady(player1, new GrizzledLeotau());
        Permanent second = addCreatureReady(player1, new GrizzledLeotau());
        second.tap();

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        first.tap();
        second.untap();
        harness.addToBattlefield(player1, new GrizzledLeotau());
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(2);

        second.tap();
        first.untap();
        harness.addToBattlefield(player1, new GrizzledLeotau());

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(2);
    }

    @Test
    void creatureTappedInResponseDoesNotCount() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        Permanent support = addCreatureReady(player1, new GrizzledLeotau());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        support.tap();
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isZero();
    }

    @Test
    void countsItselfIfUntappedBeforeResolution() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).hasSize(1);
        paladin.untap();
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(1);
    }

    @Test
    void ignoresUntappedNoncreatures() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        harness.addToBattlefield(player1, new FieldmistBorderpost());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isZero();
    }

    @Test
    void eachPaladinGetsItsOwnAttackBoost() {
        Permanent first = addCreatureReady(player1, new KnotvinePaladin());
        Permanent second = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player1, new GrizzledLeotau());

        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, first)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, second)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, second)).isEqualTo(1);
    }

    @Test
    @DisplayName("Modifier wears off at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent paladin = addCreatureReady(player1, new KnotvinePaladin());
        addCreatureReady(player1, new GrizzledLeotau());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, paladin)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paladin)).isEqualTo(0);
    }

}
