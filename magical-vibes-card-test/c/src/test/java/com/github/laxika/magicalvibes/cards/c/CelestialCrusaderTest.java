package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.k.KherKeep;
import com.github.laxika.magicalvibes.cards.k.KnightOfTheHolyNimbus;
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

@CardUsed({CelestialCrusader.class, KnightOfTheHolyNimbus.class, AshcoatBear.class, KherKeep.class})
class CelestialCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Other white creatures controlled by either player get +1/+1")
    void boostsOtherWhiteCreatures() {
        Permanent crusader = addCreatureReady(player1, new CelestialCrusader());
        Permanent ownKnight = addCreatureReady(player1, new KnightOfTheHolyNimbus());
        Permanent opponentKnight = addCreatureReady(player2, new KnightOfTheHolyNimbus());

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownKnight)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentKnight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentKnight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nonwhite creatures are not boosted")
    void doesNotBoostNonwhiteCreatures() {
        addCreatureReady(player1, new CelestialCrusader());
        Permanent bear = addCreatureReady(player1, new AshcoatBear());

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Crusaders boost each other and their bonuses stack on other white creatures")
    void multipleCrusadersStack() {
        Permanent first = addCreatureReady(player1, new CelestialCrusader());
        Permanent second = addCreatureReady(player2, new CelestialCrusader());
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's turn and the boost begins on resolution")
    void flashesInDuringOpponentsTurn() {
        Permanent knight = addCreatureReady(player1, new KnightOfTheHolyNimbus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castFromHand(player1, new CelestialCrusader(), "{2}{W}{W}");

        harness.assertNotOnBattlefield(player1, "Celestial Crusader");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Celestial Crusader");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Split second prevents spell responses only while Crusader is on the stack")
    void preventsSpellResponseUntilResolution() {
        harness.setHand(player2, List.of(new AshcoatBear()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new CelestialCrusader(), "{2}{W}{W}");

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player2, "Ashcoat Bear");

        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Split second allows mana abilities but prevents non-mana activated abilities")
    void allowsManaAbilitiesButPreventsNonManaAbilities() {
        Permanent manaSource = harness.addToBattlefieldAndReturn(player2, new KherKeep());
        Permanent tokenSource = harness.addToBattlefieldAndReturn(player2, new KherKeep());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castFromHand(player1, new CelestialCrusader(), "{2}{W}{W}");

        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(manaSource.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tokenSource.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flying prevents ground creatures from blocking Crusader but allows flying blockers")
    void flyingRestrictsBlockers() {
        Permanent attacker = addCreatureReady(player1, new CelestialCrusader());
        Permanent bear = addCreatureReady(player2, new AshcoatBear());
        Permanent flyingBlocker = addCreatureReady(player2, new CelestialCrusader());
        List<Permanent> defenders = gd.playerBattlefields.get(player2.getId());

        assertThat(bls.canBlockAttacker(gd, bear, attacker, defenders)).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, attacker, defenders)).isTrue();
    }
}
