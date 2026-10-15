package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.j.JirinaDauntlessGeneral;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSingularity;
import com.github.laxika.magicalvibes.cards.n.NowhereToRun;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoldForgedThopteryx.class, GrizzlyBears.class, Shock.class,
        JirinaDauntlessGeneral.class, LeylineOfSingularity.class, NowhereToRun.class, ProdigalPyromancer.class,
        LoxodonWarhammer.class, Naturalize.class})
class GoldForgedThopteryxTest extends BaseCardTest {

    @Test
    @DisplayName("Ward 2 protects legendary permanents you control")
    void wardProtectsLegendaryPermanent() {
        Permanent legendary = addReadyPermanent(player1, legendaryBears());
        addReadyPermanent(player1, new GoldForgedThopteryx());

        castShockAt(player2, legendary);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(legendary);
    }

    @Test
    @DisplayName("Ward 2 does not protect nonlegendary permanents")
    void wardDoesNotProtectNonlegendaryPermanent() {
        Permanent bears = addReadyPermanent(player1, new GrizzlyBears());
        addReadyPermanent(player1, new GoldForgedThopteryx());

        castShockAt(player2, bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Paying ward 2 lets a spell targeting a legendary permanent resolve")
    void payingWardLetsSpellResolve() {
        Permanent legendary = addReadyPermanent(player1, legendaryBears());
        addReadyPermanent(player1, new GoldForgedThopteryx());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, legendary.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void wardCountersOpponentActivatedAbility() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        harness.addToBattlefield(player1, new GoldForgedThopteryx());
        addReadyPermanent(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, legendary.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(legendary.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    void wardDoesNotCounterOwnSpell() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        harness.addToBattlefield(player1, new GoldForgedThopteryx());

        castShockAt(player1, legendary);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jirina, Dauntless General");
    }

    @Test
    void wardDoesNotProtectOpponentLegendaryPermanent() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new JirinaDauntlessGeneral());
        harness.addToBattlefield(player1, new GoldForgedThopteryx());

        castShockAt(player1, legendary);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jirina, Dauntless General");
    }

    @Test
    void nowhereToRunSuppressesGrantedWard() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        harness.addToBattlefield(player1, new GoldForgedThopteryx());
        harness.addToBattlefield(player2, new NowhereToRun());

        castShockAt(player2, legendary);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jirina, Dauntless General");
    }

    @Test
    void legendaryThopteryxGrantsWardToItself() {
        harness.addToBattlefield(player1, new LeylineOfSingularity());
        Permanent thopteryx = harness.addToBattlefieldAndReturn(player1, new GoldForgedThopteryx());

        castShockAt(player2, thopteryx);
        harness.passBothPriorities();

        assertThat(thopteryx.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void removingThopteryxDoesNotRemovePendingWardTrigger() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        Permanent thopteryx = harness.addToBattlefieldAndReturn(player1, new GoldForgedThopteryx());
        castShockAt(player2, legendary);
        gd.playerBattlefields.get(player1.getId()).remove(thopteryx);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(legendary);
        assertThat(legendary.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }


    @Test
    void legendaryEquipmentWardProtectsEquipmentItself() {
        harness.addToBattlefield(player1, new LeylineOfSingularity());
        harness.addToBattlefield(player1, new GoldForgedThopteryx());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        equipment.setAttachedTo(bears.getId());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, equipment.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Loxodon Warhammer");
        harness.assertInGraveyard(player2, "Naturalize");
    }

    @Test
    void legendaryEquipmentWardDoesNotTriggerForEquippedCreature() {
        harness.addToBattlefield(player1, new LeylineOfSingularity());
        harness.addToBattlefield(player1, new GoldForgedThopteryx());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        equipment.setAttachedTo(bears.getId());

        castShockAt(player2, bears);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    private GrizzlyBears legendaryBears() {
        GrizzlyBears bears = new GrizzlyBears();
        bears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return bears;
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
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
