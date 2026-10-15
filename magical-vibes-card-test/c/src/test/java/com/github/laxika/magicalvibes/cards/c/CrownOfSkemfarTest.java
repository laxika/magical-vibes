package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrownOfSkemfar.class, ElvishWarrior.class, Forest.class, GoblinBrigand.class})
class CrownOfSkemfarTest extends BaseCardTest {

    @Test
    void boostsEnchantedCreatureForEachElfAndGrantsReach() {
        Permanent enchantedCreature = addCreatureReady(player1, new GoblinBrigand());
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        int basePower = gqs.getEffectivePower(gd, enchantedCreature);
        int baseToughness = gqs.getEffectiveToughness(gd, enchantedCreature);

        harness.setHand(player1, List.of(new CrownOfSkemfar()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(baseToughness + 2);
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.REACH)).isTrue();

        Permanent thirdElf = addCreatureReady(player1, new ElvishWarrior());
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(basePower + 3);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(baseToughness + 3);

        gd.playerBattlefields.get(player1.getId()).remove(thirdElf);
        assertThat(gqs.getEffectivePower(gd, enchantedCreature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, enchantedCreature)).isEqualTo(baseToughness + 2);
    }

    @Test
    void canEnchantOnlyAcreature() {
        harness.setHand(player1, List.of(new CrownOfSkemfar()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsFromGraveyardToHand() {
        harness.setGraveyard(player1, List.of(new CrownOfSkemfar()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Crown of Skemfar");
        harness.assertNotInGraveyard(player1, "Crown of Skemfar");
    }
}
