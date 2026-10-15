package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvacynsPilgrim;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.f.FiresOfUndeath;
import com.github.laxika.magicalvibes.cards.s.SilverclawGriffin;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MikaeusTheUnhallowed.class, AvacynsPilgrim.class, EliteVanguard.class,
        GrizzlyBears.class, LightningBolt.class, WalkingCorpse.class, FiresOfUndeath.class,
        SilverclawGriffin.class, TragicSlip.class, TurnToFrog.class, YoungWolf.class,
        ProdigalPyromancer.class})
class MikaeusTheUnhallowedTest extends BaseCardTest {

    @Test
    @DisplayName("Other non-Human creatures you control get +1/+1 and undying")
    void otherNonHumanCreaturesGetBoostAndUndying() {
        Permanent mikaeus = harness.addToBattlefieldAndReturn(player1, new MikaeusTheUnhallowed());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new AvacynsPilgrim());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.UNDYING)).isTrue();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.UNDYING)).isFalse();

        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.UNDYING)).isFalse();

        assertThat(gqs.getEffectivePower(gd, mikaeus)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mikaeus)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, mikaeus, Keyword.UNDYING)).isFalse();
    }

    @Test
    @DisplayName("Granted undying returns another non-Human creature with a +1/+1 counter")
    void grantedUndyingReturnsNonHumanCreature() {
        harness.addToBattlefield(player1, new MikaeusTheUnhallowed());
        harness.addToBattlefield(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Walking Corpse"));
        resolveAllTriggers();
        assertThat(gd.stack).isEmpty();

        Permanent returned = findPermanent(player1, "Walking Corpse");
        assertThat(returned).isNotNull();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
    }

    @Test
    @DisplayName("Human that deals combat damage to Mikaeus's controller is destroyed")
    void humanDamageSourceIsDestroyed() {
        harness.addToBattlefield(player2, new MikaeusTheUnhallowed());
        Permanent attacker = addCreatureReady(player1, new EliteVanguard());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Elite Vanguard");
        harness.assertInGraveyard(player1, "Elite Vanguard");
    }

    @Test
    @DisplayName("Non-Human that deals combat damage to Mikaeus's controller is not destroyed")
    void nonHumanDamageSourceIsNotDestroyed() {
        harness.addToBattlefield(player2, new MikaeusTheUnhallowed());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void ownHumanDealingNoncombatDamageIsDestroyed() {
        harness.addToBattlefield(player1, new MikaeusTheUnhallowed());
        addCreatureReady(player1, new ProdigalPyromancer());

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertNotOnBattlefield(player1, "Prodigal Pyromancer");
        harness.assertInGraveyard(player1, "Prodigal Pyromancer");
    }

    @Test
    void innateAndGrantedUndyingTriggerIndependently() {
        harness.addToBattlefield(player1, new MikaeusTheUnhallowed());
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new YoungWolf());
        harness.setHand(player2, List.of(new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, wolf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Young Wolf");
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Young Wolf").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void humanDamageDoesNotTriggerAfterMikaeusLosesAbilities() {
        Permanent mikaeus = harness.addToBattlefieldAndReturn(player2, new MikaeusTheUnhallowed());
        Permanent attacker = addCreatureReady(player1, new EliteVanguard());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, mikaeus.getId());
        resolveAllTriggers();

        attacker.setAttacking(true);
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Elite Vanguard");
    }

    @Test
    void creatureWithPlusOneCounterDoesNotReturn() {
        harness.addToBattlefield(player1, new MikaeusTheUnhallowed());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SilverclawGriffin());
        griffin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new FiresOfUndeath(), new FiresOfUndeath()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castInstant(player2, 0, griffin.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, griffin.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Silverclaw Griffin");
        harness.assertInGraveyard(player1, "Silverclaw Griffin");
    }

    @Test
    void grantedUndyingResolvesAfterMikaeusLeavesBattlefield() {
        Permanent mikaeus = harness.addToBattlefieldAndReturn(player1, new MikaeusTheUnhallowed());
        Permanent griffin = harness.addToBattlefieldAndReturn(player1, new SilverclawGriffin());
        harness.setHand(player2, List.of(new FiresOfUndeath(), new FiresOfUndeath(), new TragicSlip()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castInstant(player2, 0, griffin.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, griffin.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Silverclaw Griffin");

        harness.castInstant(player2, 0, mikaeus.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Mikaeus, the Unhallowed");
        Permanent returned = findPermanent(player1, "Silverclaw Griffin");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.UNDYING)).isFalse();
    }
}
