package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GuanYuSaintedWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiuBeiLordOfShuTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/4 with no named permanent")
    void baseStats() {
        harness.addToBattlefield(player1, new LiuBeiLordOfShu());

        Permanent liuBei = findPermanent(player1, "Liu Bei, Lord of Shu");
        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost from an irrelevant creature")
    void noBoostFromIrrelevantCreature() {
        harness.addToBattlefield(player1, new LiuBeiLordOfShu());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent liuBei = findPermanent(player1, "Liu Bei, Lord of Shu");
        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+2 while controlling Guan Yu, Sainted Warrior")
    void boostFromGuanYu() {
        harness.addToBattlefield(player1, new LiuBeiLordOfShu());
        harness.addToBattlefield(player1, new GuanYuSaintedWarrior());

        Permanent liuBei = findPermanent(player1, "Liu Bei, Lord of Shu");
        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(6);
    }

    @Test
    @DisplayName("Gets +2/+2 while controlling a permanent named Zhang Fei, Fierce Warrior")
    void boostFromZhangFei() {
        harness.addToBattlefield(player1, new LiuBeiLordOfShu());
        harness.addToBattlefield(player1, namedCreature("Zhang Fei, Fierce Warrior"));

        Permanent liuBei = findPermanent(player1, "Liu Bei, Lord of Shu");
        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost does not stack across both named permanents")
    void boostDoesNotStack() {
        harness.addToBattlefield(player1, new LiuBeiLordOfShu());
        harness.addToBattlefield(player1, new GuanYuSaintedWarrior());
        harness.addToBattlefield(player1, namedCreature("Zhang Fei, Fierce Warrior"));

        Permanent liuBei = findPermanent(player1, "Liu Bei, Lord of Shu");
        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(4); // 2 base + 2, not +4
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(6);
    }

    @Test
    @DisplayName("Opponent's Guan Yu does not grant the boost")
    void opponentGuanYuDoesNotCount() {
        harness.addToBattlefield(player1, new LiuBeiLordOfShu());
        harness.addToBattlefield(player2, new GuanYuSaintedWarrior());

        Permanent liuBei = findPermanent(player1, "Liu Bei, Lord of Shu");
        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(4);
    }

    @Test
    @DisplayName("Loses the boost when the named permanent leaves the battlefield")
    void losesBoostWhenNamedLeaves() {
        harness.addToBattlefield(player1, new LiuBeiLordOfShu());
        harness.addToBattlefield(player1, new GuanYuSaintedWarrior());

        Permanent liuBei = findPermanent(player1, "Liu Bei, Lord of Shu");
        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Guan Yu, Sainted Warrior"));

        assertThat(gqs.getEffectivePower(gd, liuBei)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, liuBei)).isEqualTo(4);
    }

    private Card namedCreature(String name) {
        Card card = new GrizzlyBears();
        card.setName(name);
        return card;
    }

}
