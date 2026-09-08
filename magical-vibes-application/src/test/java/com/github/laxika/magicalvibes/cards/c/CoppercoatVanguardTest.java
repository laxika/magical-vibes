package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CoppercoatVanguard.class, EliteVanguard.class, GrizzlyBears.class, Shock.class})
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

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
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
