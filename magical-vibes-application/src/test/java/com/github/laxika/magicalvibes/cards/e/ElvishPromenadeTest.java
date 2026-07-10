package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElvishPromenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Elf Warrior token for each Elf controlled")
    void createsTokenPerElf() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears()); // not an Elf
        harness.setHand(player1, List.of(new ElvishPromenade()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.WARRIOR))
                .hasSize(2)
                .allMatch(p -> p.getCard().getPower() == 1 && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.ELF));
    }

    @Test
    @DisplayName("Creates no tokens when no Elves are controlled")
    void createsNoTokensWithoutElves() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // not an Elf
        harness.setHand(player1, List.of(new ElvishPromenade()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.WARRIOR));
    }
}
