package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakshaGoldenCub.class, SkyhunterProwler.class, DrossCrocodile.class, ParadiseMantle.class})
class RakshaGoldenCubTest extends BaseCardTest {

    @Test
    @DisplayName("While equipped, Raksha boosts all Cats you control and grants them double strike")
    void whileEquippedBoostsCatsAndGrantsDoubleStrike() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new SkyhunterProwler());
        Permanent crocodile = addCreatureReady(player1, new DrossCrocodile());
        attachEquipment(player1, raksha);

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, crocodile, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Without an equipped Raksha, Cats receive no bonus")
    void withoutEquipmentCatsReceiveNoBonus() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new SkyhunterProwler());

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Detaching the Equipment removes Raksha's Cat bonus")
    void detachingEquipmentRemovesBonus() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new SkyhunterProwler());
        Permanent equipment = attachEquipment(player1, raksha);

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Raksha's ability does not affect Cats controlled by an opponent")
    void doesNotAffectOpponentsCats() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent opponentCat = addCreatureReady(player2, new SkyhunterProwler());
        attachEquipment(player1, raksha);

        assertThat(gqs.getEffectivePower(gd, opponentCat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentCat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentCat, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An Equipment attached to another creature does not enable Raksha")
    void equipmentMustBeAttachedToRaksha() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new SkyhunterProwler());
        attachEquipment(player1, cat);

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Raksha is enabled by an Equipment controlled by an opponent")
    void opponentControlledEquipmentStillEquipsRaksha() {
        Permanent raksha = addCreatureReady(player1, new RakshaGoldenCub());
        Permanent cat = addCreatureReady(player1, new SkyhunterProwler());
        attachEquipment(player2, raksha);

        assertThat(gqs.getEffectivePower(gd, raksha)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, raksha)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, raksha, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cat)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cat)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, cat, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    private Permanent attachEquipment(Player player, Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new ParadiseMantle());
        equipment.setAttachedTo(creature.getId());
        return equipment;
    }
}
