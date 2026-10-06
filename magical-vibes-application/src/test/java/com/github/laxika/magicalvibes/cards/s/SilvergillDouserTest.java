package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.f.FaerieHarbinger;
import com.github.laxika.magicalvibes.cards.f.FaerieTauntings;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilvergillDouser.class, SilvergillAdept.class, FaerieHarbinger.class, HillcomberGiant.class,
        AmoeboidChangeling.class, FaerieTauntings.class})
class SilvergillDouserTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces target power by the number of Merfolk and/or Faeries controlled")
    void reducesByMerfolkAndFaerieCount() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player1, new SilvergillAdept());
        harness.addToBattlefield(player1, new FaerieHarbinger());
        // Player1 controls 3 counted permanents: Douser (Merfolk), Silvergill Adept, Faerie Harbinger

        harness.addToBattlefield(player2, new HillcomberGiant());
        UUID targetId = harness.getPermanentId(player2, "Hillcomber Giant");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Hillcomber Giant");
        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only Merfolk and Faeries, not other creatures")
    void countsOnlyMerfolkAndFaeries() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player1, new HillcomberGiant());
        // Only the Douser itself counts (1); the Hillcomber Giant does not

        harness.addToBattlefield(player2, new HillcomberGiant());
        UUID targetId = harness.getPermanentId(player2, "Hillcomber Giant");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Hillcomber Giant");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts only Merfolk and Faeries controlled by the ability's controller")
    void countsOnlyControllerCreatures() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player2, new FaerieHarbinger());
        harness.addToBattlefield(player2, new HillcomberGiant());
        UUID targetId = harness.getPermanentId(player2, "Hillcomber Giant");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Hillcomber Giant");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Uses the Merfolk and Faerie count when the ability resolves")
    void countsAtResolution() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player2, new HillcomberGiant());
        UUID targetId = harness.getPermanentId(player2, "Hillcomber Giant");

        harness.activateAbility(player1, 0, null, targetId);
        harness.addToBattlefield(player1, new SilvergillAdept());
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Hillcomber Giant");
        assertThat(target.getPowerModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Can target a creature controlled by the ability's controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new SilvergillDouser());
        UUID targetId = harness.getPermanentId(player1, "Silvergill Douser");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Silvergill Douser");
        assertThat(target.getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Debuff wears off at cleanup")
    void debuffWearsOff() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player2, new HillcomberGiant());
        UUID targetId = harness.getPermanentId(player2, "Hillcomber Giant");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Hillcomber Giant");
        assertThat(target.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player2, new HillcomberGiant());
        UUID targetId = harness.getPermanentId(player2, "Hillcomber Giant");
        harness.activateAbility(player1, 0, null, targetId);

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Counts a changeling only once even though it is both Merfolk and Faerie")
    void countsChangelingOnce() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player2, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Hillcomber Giant"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hillcomber Giant").getPowerModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Counts a Faerie permanent that is not a creature")
    void countsKindredEnchantment() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player1, new FaerieTauntings());
        harness.addToBattlefield(player2, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Hillcomber Giant"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hillcomber Giant").getPowerModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Ability still resolves with zero counted permanents after the Douser leaves")
    void resolvesWithZeroAfterSourceLeaves() {
        Permanent douser = addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player2, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Hillcomber Giant"));
        assertThat(douser.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(douser);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hillcomber Giant").getPowerModifier()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isFalse();
    }

    @Test
    @DisplayName("Reduction remains fixed when counted permanents leave after resolution")
    void reductionStaysFixedAfterResolution() {
        addCreatureReady(player1, new SilvergillDouser());
        harness.addToBattlefield(player1, new SilvergillAdept());
        harness.addToBattlefield(player2, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Hillcomber Giant"));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Hillcomber Giant"))).isEqualTo(1);
        assertThat(findPermanent(player2, "Hillcomber Giant").getToughnessModifier()).isZero();
    }

}
