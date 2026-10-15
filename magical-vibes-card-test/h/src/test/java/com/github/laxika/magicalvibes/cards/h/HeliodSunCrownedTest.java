package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeliodSunCrowned.class, AngelOfMercy.class, GrizzlyBears.class, SuntailHawk.class,
        MycosynthLattice.class, Unsummon.class})
class HeliodSunCrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Heliod is not a creature below five devotion to white")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent heliod = addHeliod();

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Heliod becomes a creature at five devotion to white")
    void becomesCreatureAtDevotionThreshold() {
        Permanent heliod = addHeliod();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }

        assertThat(gqs.isCreature(gd, heliod)).isTrue();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("Life gain puts a +1/+1 counter on a target creature or enchantment you control")
    void lifeGainCountersTargetEnchantment() {
        Permanent heliod = addHeliod();
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, heliod.getId());
        harness.passBothPriorities();

        assertThat(heliod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gain can target a creature you control")
    void lifeGainCountersTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addHeliod();
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The activated ability grants lifelink to another creature until end of turn")
    void grantsLifelinkToAnotherCreature() {
        addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot target Heliod itself")
    void cannotTargetHeliodItself() {
        Permanent heliod = addHeliod();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }
        assertThat(gqs.isCreature(gd, heliod)).isTrue();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, heliod.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Heliod stops being a creature when white devotion falls below five")
    void stopsBeingCreatureWhenDevotionFalls() {
        Permanent heliod = addHeliod();
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }
        assertThat(gqs.isCreature(gd, heliod)).isTrue();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, hawk.getId());

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
    }

    @Test
    @DisplayName("The activated ability can grant lifelink to an opponent's creature")
    void grantsLifelinkToOpponentsCreature() {
        addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Granted lifelink expires at end of turn")
    void lifelinkExpiresAtEndOfTurn() {
        addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The life gain trigger cannot target an opponent's creature")
    void lifeGainCannotCounterOpponentsCreature() {
        Permanent heliod = addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, heliod.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(heliod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent gaining life does not trigger Heliod")
    void opponentsLifeGainDoesNotTrigger() {
        Permanent heliod = addHeliod();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(heliod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Heliod keeps its counters when it becomes a creature and then stops being one")
    void countersSurviveDevotionChanges() {
        Permanent heliod = addHeliod();
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, heliod.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, heliod)).isFalse();

        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player1, new SuntailHawk());
        }
        assertThat(gqs.isCreature(gd, heliod)).isTrue();
        assertThat(gqs.getEffectivePower(gd, heliod)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, heliod)).isEqualTo(6);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, hawk.getId());

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(heliod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A life gain trigger does not place a counter on a target that leaves the battlefield")
    void lifeGainTriggerLosesItsTarget() {
        Permanent heliod = addHeliod();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new AngelOfMercy(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(heliod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Heliod retains artifact when low devotion removes creature")
    void lowDevotionPreservesOtherCardTypes() {
        harness.enterBattlefieldAndReturn(player1, new MycosynthLattice());
        Permanent heliod = harness.enterBattlefieldAndReturn(player1, new HeliodSunCrowned());

        assertThat(gqs.isCreature(gd, heliod)).isFalse();
        assertThat(gqs.isEnchantment(gd, heliod)).isTrue();
        assertThat(gqs.isArtifact(gd, heliod)).isTrue();
    }

    private Permanent addHeliod() {
        return harness.addToBattlefieldAndReturn(player1, new HeliodSunCrowned());
    }
}
