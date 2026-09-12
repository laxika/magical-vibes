package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlastedLandscape;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanarBirth.class, Forest.class, BlastedLandscape.class, CoralMerfolk.class,
        Island.class, Mountain.class, Plains.class})
class PlanarBirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all basic lands from both graveyards tapped under their owners' control")
    void returnsBasicLandsTappedUnderTheirOwnersControl() {
        Card plains = new Plains();
        Card island = new Island();
        Card mountain = new Mountain();
        harness.setGraveyard(player1, new ArrayList<>(List.of(plains, island)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(mountain)));

        castPlanarBirth();

        GameData gd = harness.getGameData();
        assertThat(battlefieldCards(player1)).containsExactlyInAnyOrder(plains, island);
        assertThat(battlefieldCards(player2)).containsExactly(mountain);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::isTapped)
                .containsExactly(true, true);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::isTapped)
                .containsExactly(true);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns only basic land cards")
    void returnsOnlyBasicLands() {
        Card basicLand = new Forest();
        Card nonbasicLand = new BlastedLandscape();
        Card creature = new CoralMerfolk();
        harness.setGraveyard(player1, new ArrayList<>(List.of(basicLand, nonbasicLand, creature)));

        castPlanarBirth();

        assertThat(battlefieldCards(player1)).containsExactly(basicLand);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(nonbasicLand, creature)
                .doesNotContain(basicLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().hasType(CardType.LAND))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Returns a basic land under its owner's control when it is in another graveyard")
    void returnsBasicLandUnderItsOwnersControlWhenInAnotherGraveyard() {
        Card landOwnedByPlayer2 = new Plains();
        landOwnedByPlayer2.setOwnerId(player2.getId());
        harness.setGraveyard(player1, List.of(landOwnedByPlayer2));

        castPlanarBirth();

        assertThat(battlefieldCards(player1)).doesNotContain(landOwnedByPlayer2);
        assertThat(battlefieldCards(player2)).containsExactly(landOwnedByPlayer2);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::isTapped)
                .containsExactly(true);
    }

    private void castPlanarBirth() {
        harness.castFromHand(player1, new PlanarBirth(), "{1}{W}");
        harness.passBothPriorities();
    }

    private List<Card> battlefieldCards(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(Permanent::getCard)
                .toList();
    }
}
