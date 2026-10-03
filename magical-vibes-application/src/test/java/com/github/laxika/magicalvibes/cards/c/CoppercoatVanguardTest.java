package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoppercoatVanguard.class, EliteVanguard.class, GrizzlyBears.class, Shock.class,
        ProdigalPyromancer.class})
class CoppercoatVanguardTest extends BaseCardTest {

    @Test
    @DisplayName("Other Humans you control get +1/+0")
    void buffsOtherHumansYouControl() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new CoppercoatVanguard());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Coppercoat Vanguard does not boost itself")
    void doesNotBoostItself() {
        Permanent vanguard = addReadyCreature(player1, new CoppercoatVanguard());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell targeting another Human when they do not pay")
    void wardCountersUnpaidSpellTargetingHuman() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());

        castShockAt(player2, human);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(human);
    }

    @Test
    @DisplayName("Ward does not trigger for a non-Human creature")
    void wardDoesNotTriggerForNonHuman() {
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        addReadyCreature(player1, new CoppercoatVanguard());

        castShockAt(player2, bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when they pay")
    void wardAllowsPaidSpell() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, human.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Elite Vanguard");
    }

    @Test
    @DisplayName("A lone Vanguard does not have ward from its own ability")
    void doesNotGrantWardToItself() {
        Permanent vanguard = addReadyCreature(player1, new CoppercoatVanguard());

        castShockAt(player2, vanguard);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coppercoat Vanguard");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Your own spell targeting a protected Human does not trigger ward")
    void wardDoesNotCounterOwnSpell() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());

        castShockAt(player1, human);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Opposing Humans receive neither the boost nor ward")
    void doesNotAffectOpposingHumans() {
        Permanent human = addReadyCreature(player2, new EliteVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        castShockAt(player1, human);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Two Vanguards boost each other and their boosts stack on another Human")
    void multipleVanguardsStackBoosts() {
        Permanent first = addReadyCreature(player1, new CoppercoatVanguard());
        Permanent second = addReadyCreature(player1, new CoppercoatVanguard());
        Permanent human = addReadyCreature(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Vanguard grants a separate ward payment")
    void multipleWardAbilitiesRequireSeparatePayments() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, human.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(human);
    }

    @Test
    @DisplayName("Ward counters an opponent's activated ability without payment")
    void wardCountersUnpaidActivatedAbility() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());
        addReadyCreature(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, human.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(human);
        assertThat(human.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent can pay ward for an activated ability")
    void wardAllowsPaidActivatedAbility() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        addReadyCreature(player1, new CoppercoatVanguard());
        addReadyCreature(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, human.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    @DisplayName("A ward trigger survives the Vanguard leaving the battlefield")
    void wardAlreadyTriggeredSurvivesVanguardLeaving() {
        Permanent human = addReadyCreature(player1, new EliteVanguard());
        Permanent vanguard = addReadyCreature(player1, new CoppercoatVanguard());
        castShockAt(player2, human);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, vanguard.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Coppercoat Vanguard");
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(human);
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castShockAt(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
    }
}
