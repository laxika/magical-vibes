package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.ToxicStench;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiberatedDwarf.class, GiantWarthog.class, SuntailHawk.class, KrosanVerge.class,
        IronshellBeetle.class, ToxicStench.class})
class LiberatedDwarfTest extends BaseCardTest {

    @Test
    void sacrificesItselfAndBoostsGreenCreatureWithFirstStrike() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(5);
        assertThat(warthog.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        harness.assertInGraveyard(player1, "Liberated Dwarf");
    }

    @Test
    void boostAndFirstStrikeWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent warthog = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, warthog.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warthog)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, warthog)).isEqualTo(5);
        assertThat(warthog.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    void cannotTargetNonGreenCreature() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hawk.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Liberated Dwarf");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityFizzlesWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new LiberatedDwarf());
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new IronshellBeetle());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, beetle.getId());

        harness.setHand(player2, java.util.List.of(new ToxicStench()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, beetle.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Liberated Dwarf");
        harness.assertInGraveyard(player2, "Ironshell Beetle");
    }
}
