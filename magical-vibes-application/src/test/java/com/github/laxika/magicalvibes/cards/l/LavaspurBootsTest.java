package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({LavaspurBoots.class, GrizzlyBears.class, Shock.class})
class LavaspurBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and haste")
    void equippedCreatureGetsBoostAndHaste() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equip ability attaches the Boots to a creature")
    void equipAttachesBoots() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when they pay")
    void wardAllowsPaidSpell() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        boots.setAttachedTo(bears.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyBoots(Player player) {
        Permanent boots = new Permanent(new LavaspurBoots());
        boots.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(boots);
        return boots;
    }
}
