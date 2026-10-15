package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.ValakutTheMoltenPinnacle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SplendidReclamation.class, Forest.class, Island.class, Mountain.class,
        GrizzlyBears.class, ValakutTheMoltenPinnacle.class})
class SplendidReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all lands from your graveyard to the battlefield tapped")
    void returnsAllLandsFromYourGraveyardTapped() {
        Card forest = new Forest();
        Card island = new Island();
        Card creature = new GrizzlyBears();
        Card opponentMountain = new Mountain();
        harness.setGraveyard(player1, new ArrayList<>(List.of(forest, island, creature)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentMountain)));

        castSplendidReclamation();

        assertThat(battlefieldCards(player1)).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(forest, island);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentMountain);
        assertThat(battlefieldCards(player2)).isEmpty();
    }

    @Test
    @DisplayName("Resolves with an empty graveyard")
    void resolvesWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());

        castSplendidReclamation();

        assertThat(battlefieldCards(player1)).isEmpty();
        harness.assertInGraveyard(player1, "Splendid Reclamation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returns lands present at resolution rather than at casting")
    void usesGraveyardAtResolution() {
        Card forest = new Forest();
        Card island = new Island();
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(new SplendidReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.setGraveyard(player1, List.of(island));
        harness.setExile(player1, List.of(forest));

        harness.passBothPriorities();

        assertThat(battlefieldCards(player1)).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Splendid Reclamation");
    }

    @Test
    @DisplayName("Simultaneously returned Mountains each satisfy Valakut's condition")
    void returnedMountainsEnterSimultaneously() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain()));
        harness.setLife(player2, 20);

        castSplendidReclamation();

        for (int i = 0; i < 2; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, player2.getId());
        }
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.assertLife(player2, 14);
    }

    private void castSplendidReclamation() {
        harness.setHand(player1, List.of(new SplendidReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private List<Card> battlefieldCards(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(Permanent::getCard)
                .toList();
    }
}
