package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TangleweaveArmor.class, EdgarMarkov.class, GrizzlyBears.class})
class TangleweaveArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates and equips a Germ using commander mana value")
    void livingWeaponCreatesAndEquipsGerm() {
        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        harness.setHand(player1, List.of(new TangleweaveArmor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        Permanent armor = findPermanent(player1, "Tangleweave Armor");
        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(armor.getAttachedTo()).isEqualTo(germ.getId());
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(6);
    }

    @Test
    @DisplayName("Commander-based boost is dynamic and is zero without a commander")
    void boostUpdatesWhenCommanderIsDesignated() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent armor = addArmorReady(player1);
        armor.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        EdgarMarkov commander = new EdgarMarkov();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(8);
    }

    private Permanent addArmorReady(Player player) {
        Permanent armor = new Permanent(new TangleweaveArmor());
        armor.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(armor);
        return armor;
    }
}
