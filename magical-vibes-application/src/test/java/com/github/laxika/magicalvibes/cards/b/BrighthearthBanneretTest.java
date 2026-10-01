package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FrogtosserBanneret;
import com.github.laxika.magicalvibes.cards.m.MurmuringBosk;
import com.github.laxika.magicalvibes.cards.s.SunflareShaman;
import com.github.laxika.magicalvibes.cards.t.TaureanMauler;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrighthearthBanneret.class, BoldwyrIntimidator.class, SunflareShaman.class,
        FrogtosserBanneret.class, MurmuringBosk.class, TaureanMauler.class})
class BrighthearthBanneretTest extends BaseCardTest {

    // ===== Elemental / Warrior cost reduction =====

    @Test
    @DisplayName("Elemental spells cost {1} less to cast with Brighthearth Banneret on the battlefield")
    void elementalSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Sunflare Shaman costs {1}{R} — with {1} reduction it should cost just {R}
        harness.castFromHand(player1, new SunflareShaman(), "{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sunflare Shaman");
    }

    @Test
    @DisplayName("Warrior spells cost {1} less to cast with Brighthearth Banneret on the battlefield")
    void warriorSpellsCostOneLess() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Boldwyr Intimidator (Giant Warrior) costs {5}{R}{R} — with {1} reduction it costs {4}{R}{R} = 6 mana
        harness.castFromHand(player1, new BoldwyrIntimidator(), "{4}{R}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Boldwyr Intimidator");
    }

    @Test
    @DisplayName("A spell that is both an Elemental and a Warrior gets only one generic mana reduction")
    void spellWithBothMatchingSubtypesIsReducedOnce() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());

        // Changeling makes Taurean Mauler an Elemental and Warrior, but its {2}{R} cost should
        // only be reduced to {1}{R}, not all the way to {R}.
        harness.setHand(player1, List.of(new TaureanMauler()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Elemental/Warrior spells are not reduced by Brighthearth Banneret")
    void otherSpellsNotReduced() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Frogtosser Banneret (Goblin Rogue) costs {1}{B} — should not be reduced
        harness.setHand(player1, List.of(new FrogtosserBanneret()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Only {B} is not enough for Frogtosser Banneret's {1}{B}.
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Brighthearth Banneret does not reduce an opponent's Elemental spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Opponent's Sunflare Shaman should still cost {1}{R}
        harness.setHand(player2, List.of(new SunflareShaman()));
        harness.addMana(player2, ManaColor.RED, 1);

        // Only {R} is not enough for {1}{R} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Reinforce 1—{1}{R} =====

    @Test
    @DisplayName("Reinforce puts a +1/+1 counter on target creature")
    void reinforceBoostsTargetCreature() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrighthearthBanneret());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reinforce discards the source card to the graveyard as a cost")
    void reinforceDiscardsSourceCard() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrighthearthBanneret());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, target.getId());

        harness.assertNotInHand(player1, "Brighthearth Banneret");
        harness.assertInGraveyard(player1, "Brighthearth Banneret");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature permanent; no cost is paid")
    void reinforceRejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MurmuringBosk());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Brighthearth Banneret");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
