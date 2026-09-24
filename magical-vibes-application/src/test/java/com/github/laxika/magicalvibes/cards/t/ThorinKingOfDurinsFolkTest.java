package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ThorinKingOfDurinsFolk.class)
class ThorinKingOfDurinsFolkTest extends BaseCardTest {

    @Test
    @DisplayName("Other Dwarves get +1/+0 for each artifact token you control")
    void boostsOtherDwarvesByArtifactTokenCount() {
        Permanent thorin = harness.addToBattlefieldAndReturn(player1, new ThorinKingOfDurinsFolk());
        Permanent dwarf = addCreature(player1, "Dwarf", CardSubtype.DWARF);
        Permanent nonDwarf = addCreature(player1, "Bear", CardSubtype.BEAR);
        Permanent opponentDwarf = addCreature(player2, "Opponent Dwarf", CardSubtype.DWARF);

        assertThat(gqs.getEffectivePower(gd, thorin)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonDwarf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentDwarf)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, creature("Entering Dwarf", CardSubtype.DWARF));
        harness.passBothPriorities();

        assertThat(countArtifactTokens(player1)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, nonDwarf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentDwarf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Treasure when Thorin or another Dwarf enters")
    void createsTreasureForThorinAndDwarfEntries() {
        harness.enterBattlefieldAndReturn(player1, new ThorinKingOfDurinsFolk());
        harness.passBothPriorities();

        assertThat(countArtifactTokens(player1)).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, creature("Dwarf", CardSubtype.DWARF));
        harness.passBothPriorities();

        assertThat(countArtifactTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("A non-Dwarf entering does not create a Treasure")
    void ignoresNonDwarfEntries() {
        harness.addToBattlefield(player1, new ThorinKingOfDurinsFolk());

        harness.enterBattlefieldAndReturn(player1, creature("Bear", CardSubtype.BEAR));

        assertThat(countArtifactTokens(player1)).isZero();
    }

    private Permanent addCreature(Player player, String name, CardSubtype subtype) {
        return addCreatureReady(player, creature(name, subtype));
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private long countArtifactTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().hasType(CardType.ARTIFACT))
                .count();
    }
}
