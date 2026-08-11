package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CelestialReunionTest extends BaseCardTest {

    @Test
    void searchesAQualifiedCreatureIntoHandWhenBeholdIsNotPaid() {
        Card found = new GrizzlyBears();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == found);
    }

    @Test
    void putsFoundCreatureOntoBattlefieldWhenItMatchesBeheldType() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Card found = new AirElemental();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorceryWithBehold(player1, 0, 5, CardSubtype.ELEMENTAL,
                List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(found);
    }

    @Test
    void keepsFoundCreatureInHandWhenItDoesNotMatchBeheldType() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Card found = new GrizzlyBears();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorceryWithBehold(player1, 0, 2, CardSubtype.ELEMENTAL,
                List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == found);
    }

    private void chooseLibraryCard(int index) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(index));
    }
}
