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
        assertThatThrownBy(() -> harness.castFromHand(player1, new TaureanMauler(), "{R}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-Elemental/Warrior spells are not reduced by Brighthearth Banneret")
    void otherSpellsNotReduced() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Frogtosser Banneret (Goblin Rogue) costs {1}{B} — should not be reduced
        // Only {B} is not enough for Frogtosser Banneret's {1}{B}.
        assertThatThrownBy(() -> harness.castFromHand(player1, new FrogtosserBanneret(), "{B}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Brighthearth Banneret does not reduce an opponent's Elemental spell costs")
    void doesNotReduceOpponentCosts() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        // Opponent's Sunflare Shaman should still cost {1}{R}
        gd.activePlayerId = player2.getId();

        // Only {R} is not enough for {1}{R} — reduction does not apply to opponent
        assertThatThrownBy(() -> harness.castFromHand(player2, new SunflareShaman(), "{R}"))
                .isInstanceOf(IllegalStateException.class);
    }

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

    @Test
    @DisplayName("Changeling spells can be cast with exactly one generic mana reduction")
    void changelingSpellGetsReduction() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());

        harness.castFromHand(player1, new TaureanMauler(), "{1}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Taurean Mauler");
    }

    @Test
    @DisplayName("Cost reductions from multiple Bannerets add together")
    void multipleBanneretsStackTheirReductions() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        harness.addToBattlefield(player1, new BrighthearthBanneret());

        harness.castFromHand(player1, new TaureanMauler(), "{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Taurean Mauler");
    }

    @Test
    @DisplayName("Excess generic mana reduction cannot pay a colored mana requirement")
    void reductionsCannotRemoveColoredCost() {
        harness.addToBattlefield(player1, new BrighthearthBanneret());
        harness.addToBattlefield(player1, new BrighthearthBanneret());

        assertThatThrownBy(() -> harness.castFromHand(player1, new SunflareShaman(), "{1}"))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Sunflare Shaman");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A Banneret in hand does not reduce its own casting cost")
    void reductionRequiresBanneretOnBattlefield() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new BrighthearthBanneret(), "{R}"))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Brighthearth Banneret");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce can target an opponent's creature during their turn")
    void reinforceCanTargetOpponentCreatureOnOpponentTurn() {
        gd.activePlayerId = player2.getId();
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BrighthearthBanneret());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Brighthearth Banneret");
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell cost reduction does not reduce reinforce's activation cost")
    void spellReductionDoesNotApplyToReinforce() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrighthearthBanneret());
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Brighthearth Banneret");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Reinforce requires red mana, not just two generic mana")
    void reinforceRequiresRedMana() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BrighthearthBanneret());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Brighthearth Banneret");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reinforce requires a target before costs can be paid")
    void reinforceRequiresTarget() {
        harness.setHand(player1, List.of(new BrighthearthBanneret()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Brighthearth Banneret");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
