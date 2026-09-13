package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuagmireDruid.class, PhyrexianArena.class, UrborgElf.class})
class QuagmireDruidTest extends BaseCardTest {

    @Test
    void sacrificesCreatureTapsAndDestroysTargetEnchantment() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuagmireDruid());
        druid.setSummoningSick(false);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new UrborgElf());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, enchantment.getId());
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(druid).doesNotContain(fodder);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Urborg Elf");
        harness.assertNotOnBattlefield(player2, "Phyrexian Arena");
        harness.assertInGraveyard(player2, "Phyrexian Arena");
    }

    @Test
    void cannotTargetCreature() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuagmireDruid());
        druid.setSummoningSick(false);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new UrborgElf());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new UrborgElf());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(druid.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
    }

    @Test
    void canDestroyAnEnchantmentItControls() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuagmireDruid());
        druid.setSummoningSick(false);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new UrborgElf());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new PhyrexianArena());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, enchantment.getId());
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Arena");
        harness.assertInGraveyard(player1, "Phyrexian Arena");
    }

    @Test
    void maySacrificeItselfAsTheCreatureCost() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new QuagmireDruid());
        druid.setSummoningSick(false);
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, enchantment.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(druid);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Quagmire Druid");
        harness.assertNotOnBattlefield(player2, "Phyrexian Arena");
        harness.assertInGraveyard(player2, "Phyrexian Arena");
    }
}
