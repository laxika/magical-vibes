package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Batterbone.class, GrizzlyBears.class})
class BatterboneTest extends BaseCardTest {

    @Test
    void livingWeaponCreatesAndAttachesGerm() {
        harness.setHand(player1, List.of(new Batterbone()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent batterbone = findPermanent(player1, "Batterbone");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");

        assertThat(batterbone.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    void equippedCreatureGetsBoostAndKeywords() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent batterbone = new Permanent(new Batterbone());
        batterbone.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(batterbone);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void equipAbilityMovesBatterboneToAnotherCreature() {
        Permanent firstBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent batterbone = new Permanent(new Batterbone());
        batterbone.setAttachedTo(firstBear.getId());
        gd.playerBattlefields.get(player1.getId()).add(batterbone);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 2, null, secondBear.getId());
        harness.passBothPriorities();

        assertThat(batterbone.getAttachedTo()).isEqualTo(secondBear.getId());
        assertThat(gqs.getEffectivePower(gd, firstBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondBear)).isEqualTo(3);
    }
}
