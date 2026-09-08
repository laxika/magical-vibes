package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CatWarriors;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RakshaGoldenCubTest extends BaseCardTest {

    @Test
    @DisplayName("While equipped, Raksha boosts all Cats you control and grants them double strike")
    void whileEquippedBoostsCatsAndGrantsDoubleStrike() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new CatWarriors());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(player1, raksha);

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Without an equipped Raksha, Cats receive no bonus")
    void withoutEquipmentCatsReceiveNoBonus() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new CatWarriors());

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Detaching the Equipment removes Raksha's Cat bonus")
    void detachingEquipmentRemovesBonus() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new CatWarriors());
        Permanent equipment = attachEquipment(player1, raksha);

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Raksha's ability does not affect Cats controlled by an opponent")
    void doesNotAffectOpponentsCats() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent opponentCat = addCreatureReady(player2, new CatWarriors());
        attachEquipment(player1, raksha);

        assertThat(gqs.getEffectivePower(gd, opponentCat)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCat)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCat, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent attachEquipment(Player player, Permanent creature) {
        Permanent equipment = new Permanent(new SwiftfootBoots());
        gd.playerBattlefields.get(player.getId()).add(equipment);
        equipment.setAttachedTo(creature.getId());
        return equipment;
    }
}
