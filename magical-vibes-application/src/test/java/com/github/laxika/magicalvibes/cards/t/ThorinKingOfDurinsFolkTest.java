package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.d.DwarvenWarriors;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
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

@CardUsed({ThorinKingOfDurinsFolk.class, Conspiracy.class, DwarvenWarriors.class,
        WayfarersBauble.class})
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

    @Test
    @DisplayName("Thorin creates a Treasure on its own entry even when its creature types are replaced")
    void selfEntryDoesNotRequireDwarfSubtype() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        harness.castFromHand(player1, new ThorinKingOfDurinsFolk(), "{3}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countArtifactTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Dwarf entering does not create a Treasure")
    void ignoresOpponentDwarfEntries() {
        harness.addToBattlefield(player1, new ThorinKingOfDurinsFolk());

        harness.enterBattlefieldAndReturn(player2, new DwarvenWarriors());

        assertThat(gd.stack).isEmpty();
        assertThat(countArtifactTokens(player1)).isZero();
        assertThat(countArtifactTokens(player2)).isZero();
    }

    @Test
    @DisplayName("Dwarf tokens also trigger Treasure creation and receive the boost")
    void dwarfTokenEntryCreatesTreasureAndReceivesBoost() {
        Permanent thorin = harness.addToBattlefieldAndReturn(player1, new ThorinKingOfDurinsFolk());
        Card dwarfToken = new DwarvenWarriors().createRuntimeCopy();
        dwarfToken.setToken(true);

        Permanent dwarf = harness.enterBattlefieldAndReturn(player1, dwarfToken);
        harness.passBothPriorities();

        assertThat(countArtifactTokens(player1)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, thorin)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost counts all your artifact tokens, excluding nontokens and opponents' tokens")
    void countsOnlyControlledArtifactTokensAndUpdatesWithNewTreasures() {
        Permanent thorin = harness.addToBattlefieldAndReturn(player1, new ThorinKingOfDurinsFolk());
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new DwarvenWarriors());
        harness.addToBattlefield(player1, new WayfarersBauble());

        Card artifactToken = new WayfarersBauble().createRuntimeCopy();
        artifactToken.setToken(true);
        harness.addToBattlefield(player1, artifactToken);
        Card opponentArtifactToken = new WayfarersBauble().createRuntimeCopy();
        opponentArtifactToken.setToken(true);
        harness.addToBattlefield(player2, opponentArtifactToken);
        Card creatureToken = new DwarvenWarriors().createRuntimeCopy();
        creatureToken.setToken(true);
        harness.addToBattlefield(player1, creatureToken);

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, thorin)).isEqualTo(4);

        harness.enterBattlefieldAndReturn(player1, new DwarvenWarriors());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, thorin)).isEqualTo(4);
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
