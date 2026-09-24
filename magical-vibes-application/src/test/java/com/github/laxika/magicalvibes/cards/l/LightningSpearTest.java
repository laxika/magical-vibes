package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningSpear.class, GrizzlyBears.class})
class LightningSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent creature = addCreatureReady(player1);
        Permanent spear = addSpear(player1);
        spear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equip {1} attaches Lightning Spear to a creature")
    void equipAttaches() {
        Permanent spear = addSpear(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Sacrificing Lightning Spear deals 3 damage to a creature")
    void sacrificesAndDealsDamageToCreature() {
        addSpear(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addDamageMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Lightning Spear");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing Lightning Spear deals 3 damage to a player")
    void sacrificesAndDealsDamageToPlayer() {
        addSpear(player1);
        harness.setLife(player2, 20);
        addDamageMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Lightning Spear");
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    private Permanent addSpear(Player player) {
        Permanent permanent = new Permanent(new LightningSpear());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addDamageMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
