package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrenisCounsel.class, ShivanDragon.class, GrizzlyBears.class})
class UrenisCounselTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each Dragon card in its controller's library")
    void costsLessForEachDragonInLibrary() {
        Card dragon = new ShivanDragon();
        Card nonDragon = new GrizzlyBears();
        harness.setLibrary(player1, List.of(dragon, nonDragon));
        harness.setHand(player1, List.of(new UrenisCounsel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonDragon);
    }

    @Test
    @DisplayName("Harmonize casts it from the graveyard and exiles it")
    void harmonizeCastsFromGraveyard() {
        UrenisCounsel counsel = new UrenisCounsel();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashbackWithTapCost(player1, 0, List.of(creature.getId()));
        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(counsel.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
