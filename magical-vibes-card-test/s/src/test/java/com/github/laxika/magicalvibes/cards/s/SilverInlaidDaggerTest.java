package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverInlaidDagger.class, EliteVanguard.class, GrizzlyBears.class})
class SilverInlaidDaggerTest extends BaseCardTest {


    @Test
    @DisplayName("Equipped non-Human creature gets +2/+0")
    void equippedNonHumanGetsBaseBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);   // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Equipped Human creature gets +3/+0 (base +2 plus Human +1)")
    void equippedHumanGetsFullBoost() {
        Permanent human = addReadyHuman(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);   // 2 + 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1); // 1 + 0
    }


    @Test
    @DisplayName("Equipped Human deals 5 combat damage (2 base + 2 + 1)")
    void equippedHumanDealsCombatDamage() {
        harness.setLife(player2, 20);

        Permanent human = addReadyHuman(player1);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(human.getId());
        human.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15); // 20 - 5
    }

    @Test
    @DisplayName("Equipped non-Human deals 4 combat damage (2 base + 2)")
    void equippedNonHumanDealsCombatDamage() {
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16); // 20 - 4
    }


    @Test
    @DisplayName("Moving Dagger from Human to non-Human removes the Human bonus")
    void movingFromHumanToNonHumanRemovesBonus() {
        Permanent dagger = addDaggerReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        dagger.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5); // 2 + 2 + 1

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);    // back to base
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4); // 2 + 2 (no Human bonus)
    }

    @Test
    @DisplayName("Moving Dagger from non-Human to Human grants the Human bonus")
    void movingFromNonHumanToHumanGrantsBonus() {
        Permanent dagger = addDaggerReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent human = addReadyHuman(player1);
        dagger.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4); // 2 + 2

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5); // 2 + 2 + 1
    }


    @Test
    @DisplayName("Unattached Dagger does not boost any creature")
    void unattachedDaggerDoesNotBoostCreatures() {
        addDaggerReady(player1);
        Permanent human = addReadyHuman(player1);
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each attached Dagger grants its own base and Human bonuses")
    void multipleDaggersStackOnHuman() {
        Permanent human = addReadyHuman(player1);
        addDaggerReady(player1).setAttachedTo(human.getId());
        addDaggerReady(player1).setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("An attached Dagger boosts a Human even when its controller differs")
    void attachedDaggerBoostsOpponentsHuman() {
        Permanent human = addReadyHuman(player2);
        Permanent dagger = addDaggerReady(player1);
        dagger.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent dagger = addDaggerReady(player1);
        Permanent human = addReadyHuman(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(dagger.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip requires sorcery timing")
    void equipRejectsCombatTiming() {
        Permanent dagger = addDaggerReady(player1);
        Permanent human = addReadyHuman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, human.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(dagger.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip pays two mana and attaches only on resolution")
    void equipPaysCostAndResolvesThroughStack() {
        Permanent dagger = addDaggerReady(player1);
        Permanent human = addReadyHuman(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, human.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(dagger.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(dagger.getAttachedTo()).isEqualTo(human.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addDaggerReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SilverInlaidDagger());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyHuman(Player player) {
        return addCreatureReady(player, new EliteVanguard());
    }
}
