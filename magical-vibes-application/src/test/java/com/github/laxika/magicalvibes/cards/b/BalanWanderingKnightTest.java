package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalanWanderingKnight.class, BoneSaw.class, Bonesplitter.class})
class BalanWanderingKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Balan has double strike only while two or more Equipment are attached")
    void doubleStrikeRequiresTwoAttachedEquipment() {
        Permanent balan = addCreatureReady(player1, new BalanWanderingKnight());
        Permanent saw = addEquipmentReady(player1, new BoneSaw());
        saw.setAttachedTo(balan.getId());

        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isFalse();

        Permanent splitter = addEquipmentReady(player1, new Bonesplitter());
        splitter.setAttachedTo(balan.getId());
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isTrue();

        splitter.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Balan attaches all Equipment controlled by the ability's controller")
    void attachesAllControlledEquipment() {
        Permanent balan = addCreatureReady(player1, new BalanWanderingKnight());
        Permanent saw = addEquipmentReady(player1, new BoneSaw());
        Permanent splitter = addEquipmentReady(player1, new Bonesplitter());
        Permanent opponentSaw = addEquipmentReady(player2, new BoneSaw());

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(saw.getAttachedTo()).isEqualTo(balan.getId());
        assertThat(splitter.getAttachedTo()).isEqualTo(balan.getId());
        assertThat(opponentSaw.getAttachedTo()).isNull();
        assertThat(gqs.hasKeyword(gd, balan, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private Permanent addEquipmentReady(Player player, Card card) {
        Permanent equipment = new Permanent(card);
        equipment.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(equipment);
        return equipment;
    }
}
