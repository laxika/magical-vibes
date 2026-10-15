package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.ReliquaryTower;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaforceMace.class, GrizzlyBears.class, Forest.class, Island.class, Plains.class,
        Swamp.class, Mountain.class, ReliquaryTower.class})
class ManaforceMaceTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each basic land type among controlled lands")
    void boostsPerBasicLandType() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        // 2/2 base + 3 distinct basic land types * +1/+1 = 5/5
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("Duplicate basic land types are counted once")
    void duplicateBasicTypesCountOnce() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        // Three Forests = one distinct type = +1/+1 -> 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("No basic land types means no boost")
    void noBasicLandTypesNoBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Domain counts equipment controller's basic land types, not the opponent's")
    void doesNotCountOpponentLands() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Plains());

        // Only player1's single Forest type counts -> 3/3
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving equip ability attaches the mace and grants the domain boost")
    void equipAttachesAndBoosts() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int maceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mace);
        harness.activateAbility(player1, maceIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
        // 2/2 base + 2 distinct basic land types = 4/4
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equipped creature loses the boost when the mace leaves the battlefield")
    void creatureLosesBoostWhenMaceRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(mace);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Domain updates as basic land types enter and leave")
    void boostUpdatesWithLandTypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(island);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("All five basic land types grant +5/+5")
    void allFiveTypesGrantMaximumDomainBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Mountain());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
    }

    @Test
    @DisplayName("A land without basic land types contributes nothing to domain")
    void untypedNonbasicLandDoesNotBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new ReliquaryTower());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Domain uses the Mace controller even when another player controls the creature")
    void differentCreatureControllerDoesNotChangeDomainSource() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(bears.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Re-equipping moves the domain boost to the new creature")
    void reequippingMovesBoost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        mace.setAttachedTo(first.getId());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int maceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mace);
        harness.activateAbility(player1, maceIndex, null, second.getId());
        harness.passBothPriorities();

        assertThat(mace.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }
    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mace), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(mace.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void cannotEquipOutsideMainPhase() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mace), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(mace.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip spends exactly three generic mana")
    void equipPaysThreeMana() {
        Permanent mace = harness.addToBattlefieldAndReturn(player1, new ManaforceMace());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mace), null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(mace.getAttachedTo()).isEqualTo(creature.getId());
    }
}
