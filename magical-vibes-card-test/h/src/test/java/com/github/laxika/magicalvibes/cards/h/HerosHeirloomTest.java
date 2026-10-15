package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.y.YavimayaSteelcrusher;
import com.github.laxika.magicalvibes.cards.b.BairdArgivianRecruiter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerosHeirloom.class, YavimayaSteelcrusher.class, BairdArgivianRecruiter.class})
class HerosHeirloomTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        heirloom.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equipped legendary creature has trample and haste")
    void equippedLegendaryCreatureHasKeywords() {
        Permanent creature = addCreatureReady(player1, new BairdArgivianRecruiter());
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        heirloom.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Bonuses are lost when Hero's Heirloom becomes unattached")
    void bonusesAreLostWhenUnattached() {
        Permanent creature = addCreatureReady(player1, new BairdArgivianRecruiter());
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        heirloom.setAttachedTo(creature.getId());

        heirloom.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Hero's Heirloom to a creature you control")
    void equipAttachesToCreature() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(heirloom.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip grants its bonuses to a newly entered legendary creature")
    void equipLegendaryCreatureGrantsBonuses() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BairdArgivianRecruiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(heirloom.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("A failed equip leaves the previous creature equipped")
    void targetLeavingDoesNotDetachEquipment() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        Permanent original = addCreatureReady(player1, new BairdArgivianRecruiter());
        Permanent target = addCreatureReady(player1, new YavimayaSteelcrusher());
        heirloom.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(heirloom.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, original, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, original, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Reequipping transfers the boost and removes legendary-only keywords")
    void reequippingTransfersBonuses() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        Permanent legendary = addCreatureReady(player1, new BairdArgivianRecruiter());
        Permanent ordinary = addCreatureReady(player1, new YavimayaSteelcrusher());
        heirloom.setAttachedTo(legendary.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, ordinary.getId());
        assertThat(heirloom.getAttachedTo()).isEqualTo(legendary.getId());
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.HASTE)).isTrue();
        harness.passBothPriorities();

        assertThat(heirloom.getAttachedTo()).isEqualTo(ordinary.getId());
        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ordinary)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ordinary)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ordinary, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ordinary, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        Permanent creature = addCreatureReady(player2, new BairdArgivianRecruiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heirloom.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip requires two mana")
    void equipRequiresTwoMana() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heirloom.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during upkeep")
    void cannotEquipDuringUpkeep() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerosHeirloom());
        Permanent creature = addCreatureReady(player1, new YavimayaSteelcrusher());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(heirloom.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
