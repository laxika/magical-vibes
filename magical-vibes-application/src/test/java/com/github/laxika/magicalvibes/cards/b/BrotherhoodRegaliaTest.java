package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KamahlPitFighter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrotherhoodRegalia.class, GrizzlyBears.class, KamahlPitFighter.class, Shock.class})
class BrotherhoodRegaliaTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature becomes an Assassin and can't be blocked")
    void equippedCreatureGetsAssassinAndCantBeBlocked() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent regalia = addReadyRegalia(player1);
        regalia.setAttachedTo(creature.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.ASSASSIN)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Legendary equip ability attaches for {1}")
    void legendaryEquipAttachesForOne() {
        Permanent regalia = addReadyRegalia(player1);
        Permanent legendaryCreature = addCreatureReady(player1, new KamahlPitFighter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, regalia), 0, null,
                legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(regalia.getAttachedTo()).isEqualTo(legendaryCreature.getId());
    }

    @Test
    @DisplayName("Legendary equip ability rejects a nonlegendary creature")
    void legendaryEquipRejectsNonlegendaryCreature() {
        Permanent regalia = addReadyRegalia(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, regalia), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Ordinary equip ability attaches to a nonlegendary creature for {3}")
    void ordinaryEquipAttachesForThree() {
        Permanent regalia = addReadyRegalia(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, regalia), 1, null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(regalia.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent regalia = addReadyRegalia(player1);
        regalia.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Ward lets an opponent's spell resolve when they pay")
    void wardAllowsPaidSpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent regalia = addReadyRegalia(player1);
        regalia.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addReadyRegalia(Player player) {
        Permanent regalia = new Permanent(new BrotherhoodRegalia());
        regalia.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(regalia);
        return regalia;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
