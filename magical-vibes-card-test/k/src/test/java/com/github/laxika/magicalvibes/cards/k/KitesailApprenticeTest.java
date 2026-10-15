package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HammerOfRuin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KitesailApprentice.class, HammerOfRuin.class})
class KitesailApprenticeTest extends BaseCardTest {

    @Test
    void withoutEquipmentHasNoBonus() {
        Permanent apprentice = addApprentice(player1);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    void whileEquippedGetsBonusAndFlying() {
        Permanent apprentice = addApprentice(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(apprentice.getId());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
    }

    @Test
    void losesBonusWhenEquipmentIsDetached() {
        Permanent apprentice = addApprentice(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(apprentice.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
    }

    @Test
    void multipleEquipmentGrantTheApprenticeBonusOnlyOnce() {
        Permanent apprentice = addApprentice(player1);
        Permanent first = addEquipment(player1);
        Permanent second = addEquipment(player1);
        first.setAttachedTo(apprentice.getId());
        second.setAttachedTo(apprentice.getId());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();

        first.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipmentControlledByOpponentStillEnablesBonus() {
        Permanent apprentice = addApprentice(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(apprentice.getId());
        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();
    }

    @Test
    void movingEquipmentToAnotherCreatureRemovesBonus() {
        Permanent apprentice = addApprentice(player1);
        Permanent other = addApprentice(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(apprentice.getId());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isTrue();

        equipment.setAttachedTo(other.getId());

        assertThat(gqs.getEffectivePower(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, apprentice)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, apprentice, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
    }

    private Permanent addApprentice(Player player) {
        return addCreatureReady(player, new KitesailApprentice());
    }

    private Permanent addEquipment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HammerOfRuin());
    }
}
