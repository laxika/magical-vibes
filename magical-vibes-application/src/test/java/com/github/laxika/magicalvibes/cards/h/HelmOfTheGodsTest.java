package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOfTheGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each enchantment its controller controls")
    void boostsPerEnchantment() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent helm = new Permanent(new HelmOfTheGods());
        helm.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(helm);

        harness.addToBattlefield(player1, new AuraOfSilence());
        harness.addToBattlefield(player1, new AuraOfSilence());

        // 2/2 base + 2 enchantments = 4/4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost updates dynamically as enchantments enter and leave")
    void updatesDynamically() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent helm = new Permanent(new HelmOfTheGods());
        helm.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(helm);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new AuraOfSilence());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Aura of Silence"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count the opponent's enchantments")
    void ignoresOpponentEnchantments() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent helm = new Permanent(new HelmOfTheGods());
        helm.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(helm);

        harness.addToBattlefield(player2, new AuraOfSilence());
        harness.addToBattlefield(player2, new AuraOfSilence());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip {1} attaches the Helm to a creature you control")
    void equipForOne() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent helm = new Permanent(new HelmOfTheGods());
        gd.playerBattlefields.get(player1.getId()).add(helm);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        harness.activateAbility(player1, battlefield.indexOf(helm), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(helm.getAttachedTo()).isEqualTo(bears.getId());
    }
}
