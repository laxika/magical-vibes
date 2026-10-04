package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuskmantleProwler.class, WalkingCorpse.class})
class DuskmantleProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new DuskmantleProwler());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1)); // Walking Corpse attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Prowler attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent prowler = addCreatureReady(player1, new DuskmantleProwler());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new DuskmantleProwler());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new DuskmantleProwler());
        Permanent bears = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Duskmantle Prowler"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Haste allows the Prowler to attack on the turn it enters")
    void canAttackImmediatelyAfterResolving() {
        harness.castFromHand(player1, new DuskmantleProwler(), "{3}{B}");
        harness.passBothPriorities();
        Permanent prowler = findPermanent(player1, "Duskmantle Prowler");

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(prowler.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Each Prowler contributes an exalted boost to the lone attacker")
    void multipleExaltedAbilitiesStack() {
        addCreatureReady(player1, new DuskmantleProwler());
        addCreatureReady(player1, new DuskmantleProwler());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(4);
    }

    @Test
    @DisplayName("Exalted does not boost an opponent's lone attacker")
    void opponentsAttackDoesNotTriggerExalted() {
        Permanent prowler = addCreatureReady(player1, new DuskmantleProwler());
        Permanent corpse = addCreatureReady(player2, new WalkingCorpse());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted resolves even after its source leaves the battlefield")
    void exaltedResolvesWithoutItsSource() {
        Permanent prowler = addCreatureReady(player1, new DuskmantleProwler());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(prowler);
        gd.playerGraveyards.get(player1.getId()).add(prowler.getCard());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(3);
    }

    @Test
    @DisplayName("Leaving combat does not undo the event that triggered exalted")
    void exaltedStillBoostsCreatureRemovedFromCombat() {
        addCreatureReady(player1, new DuskmantleProwler());
        Permanent corpse = addCreatureReady(player1, new WalkingCorpse());

        declareAttackers(player1, List.of(1));
        assertThat(gd.stack).hasSize(1);
        corpse.setAttacking(false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, corpse)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, corpse)).isEqualTo(3);
    }
}
