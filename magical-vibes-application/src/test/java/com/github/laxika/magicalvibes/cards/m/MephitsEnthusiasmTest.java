package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MephitsEnthusiasm.class, GrizzlyBears.class, SerraAngel.class, ChandraNalaar.class, Unsummon.class})
class MephitsEnthusiasmTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetCreatureWithoutExcessDamageBoon() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    void excessDamageGivesTheNextCreatureSpellTheNotedPerpetualPowerBoostOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears.get(0))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears.get(1))).isEqualTo(2);
    }

    @Test
    void dealsFourDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        castAt(planeswalker);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new MephitsEnthusiasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    @Test
    void damageAlreadyMarkedIncreasesTheNotedExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.setMarkedDamage(3);
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        castBears();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void excessDamageToPlaneswalkerCreatesBoonUsingItsPreviousLoyalty() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 1);
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        castBears();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(5);
    }

    @Test
    void multipleBoonsBoostTheSameNextCreatureSpell() {
        castAt(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        castAt(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));

        castBears();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(6);
        castBears();
        assertThat(gqs.getEffectivePower(gd, findPermanents(player1, "Grizzly Bears").get(1))).isEqualTo(2);
    }

    @Test
    void perpetualBoostSurvivesReturningToHandAndRecasting() {
        castAt(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        castBears();
        Permanent boosted = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, boosted)).isEqualTo(4);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, boosted.getId());
        assertThat(gd.playerHands.get(player1.getId())).contains(boosted.getCard());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent recast = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, recast)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, recast)).isEqualTo(2);
    }

    @Test
    void losingTheTargetBeforeResolutionDoesNotCreateBoon() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MephitsEnthusiasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        castBears();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(2);
    }

    @Test
    void boonSurvivesTurnChangesAndIgnoresOpponentsCreatureSpells() {
        castAt(harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Grizzly Bears"))).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        castBears();
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Grizzly Bears"))).isEqualTo(4);
    }

    private void castBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
