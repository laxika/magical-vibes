package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CultivatorColossusTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals number of lands you control")
    void ptEqualsControlledLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        Permanent colossus = harness.addToBattlefieldAndReturn(player1, new CultivatorColossus());

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB puts a chosen land tapped, draws, and re-offers until declined")
    void etbPutsLandDrawsAndRepeatsUntilDeclined() {
        harness.addToBattlefield(player1, new Forest());
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 5; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new CultivatorColossus(), new Forest(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest") && p.isTapped());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Plains"));
    }

    @Test
    @DisplayName("Declining the first land put draws nothing")
    void decliningFirstPutDrawsNothing() {
        harness.addToBattlefield(player1, new Forest());
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new CultivatorColossus(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest"))
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore);
    }

    @Test
    @DisplayName("Putting all lands from hand draws that many times and ends when none remain")
    void puttingAllLandsDrawsAndEnds() {
        harness.addToBattlefield(player1, new Forest());
        gd.playerDecks.get(player1.getId()).clear();
        harness.setHand(player1, List.of(new CultivatorColossus(), new Forest(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0); // Forest; draw from empty library
        harness.handleCardChosen(player1, 0); // Plains; no lands left → process ends

        List<Permanent> lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .toList();
        assertThat(lands).hasSize(3);
        assertThat(lands.stream().filter(Permanent::isTapped).count()).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("P/T grows as lands are put onto the battlefield")
    void ptGrowsAsLandsEnter() {
        harness.addToBattlefield(player1, new Forest());
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < 3; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new CultivatorColossus(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent colossus = findPermanent(player1, "Cultivator Colossus");
        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(1);

        harness.handleCardChosen(player1, 0); // only land — process ends after draw (no lands left)

        assertThat(gqs.getEffectivePower(gd, colossus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, colossus)).isEqualTo(2);
    }
}
