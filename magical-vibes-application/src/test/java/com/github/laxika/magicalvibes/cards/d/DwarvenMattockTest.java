package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenMattock.class, DwarvenSeaClan.class, GrizzlyBears.class, Shock.class,
        ZuranSpellcaster.class})
class DwarvenMattockTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Dwarven Mattock attaches it to a target Dwarf and boosts it")
    void enteringAttachesToTargetDwarfAndBoostsIt() {
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        int powerBefore = gqs.getEffectivePower(gd, dwarf);
        int toughnessBefore = gqs.getEffectiveToughness(gd, dwarf);
        harness.setHand(player1, List.of(new DwarvenMattock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, dwarf.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mattock = findPermanent(player1, "Dwarven Mattock");
        assertThat(mattock.getAttachedTo()).isEqualTo(dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(toughnessBefore + 2);
    }

    @Test
    @DisplayName("Dwarven Mattock cannot target a non-Dwarf creature")
    void cannotTargetNonDwarfCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DwarvenMattock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Dwarf you control");
    }

    @Test
    @DisplayName("Equip {3} attaches Dwarven Mattock to a creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent mattock = addReadyMattock(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(mattock.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent mattock = addReadyMattock(player1);
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        mattock.setAttachedTo(dwarf.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, dwarf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dwarf);
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when they pay")
    void wardAllowsPaidSpell() {
        Permanent mattock = addReadyMattock(player1);
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        mattock.setAttachedTo(dwarf.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, dwarf.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(dwarf.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward counters an opponent's ability when they cannot pay")
    void wardCountersUnpaidAbility() {
        Permanent mattock = addReadyMattock(player1);
        Permanent dwarf = addCreatureReady(player1, new DwarvenSeaClan());
        mattock.setAttachedTo(dwarf.getId());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(spellcaster), null, dwarf.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(dwarf);
        assertThat(dwarf.getMarkedDamage()).isZero();
    }

    private Permanent addReadyMattock(Player player) {
        Permanent mattock = new Permanent(new DwarvenMattock());
        mattock.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mattock);
        return mattock;
    }
}
