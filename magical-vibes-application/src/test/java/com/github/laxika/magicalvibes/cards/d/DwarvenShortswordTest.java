package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DwarvenShortsword.class, GrizzlyBears.class})
class DwarvenShortswordTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Dwarven Shortsword creates and equips a red Dwarf token")
    void enteringCreatesAndEquipsDwarf() {
        harness.setHand(player1, List.of(new DwarvenShortsword()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent shortsword = findPermanent(player1, "Dwarven Shortsword");
        Permanent dwarf = findPermanent(player1, "Dwarf");
        assertThat(dwarf.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dwarf.getCard().getSubtypes()).containsExactly(CardSubtype.DWARF);
        assertThat(shortsword.getAttachedTo()).isEqualTo(dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip {2} attaches Dwarven Shortsword and gives +1/+2")
    void equipAttachesAndBoostsCreature() {
        Permanent shortsword = addReady(player1, new DwarvenShortsword());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(shortsword.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
