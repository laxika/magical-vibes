package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(DuskmantleHouseOfShadow.class)
class DuskmantleHouseOfShadowTest extends BaseCardTest {

    @Test
    void tappingProducesColorlessMana() {
        Permanent land = addReadyLand(player1);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(land));

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void activatedAbilityMillsOneCardFromTargetPlayersLibrary() {
        Permanent land = addReadyLand(player1);
        Card topCard = gd.playerDecks.get(player2.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    void activatedAbilityCanTargetItsController() {
        addReadyLand(player1);
        Card topCard = gd.playerDecks.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void activatedAbilityRequiresBothColoredMana() {
        addReadyLand(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void activatedAbilityDoesNothingWhenTargetLibraryIsEmpty() {
        Permanent land = addReadyLand(player1);
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void millingUsesTheStackAndPaysOnlyBlueAndBlackMana() {
        Permanent land = addReadyLand(player1);
        Card topCard = new DuskmantleHouseOfShadow();
        Card secondCard = new DuskmantleHouseOfShadow();
        harness.setLibrary(player2, List.of(topCard, secondCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }

    @Test
    void tappedLandCannotActivateMillingAbility() {
        Permanent land = addReadyLand(player1);
        land.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newlyEnteredNoncreatureLandCanActivateMillingAbility() {
        harness.addToBattlefield(player1, new DuskmantleHouseOfShadow());
        Card topCard = new DuskmantleHouseOfShadow();
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(topCard);
    }
    private Permanent addReadyLand(Player player) {
        Permanent land = harness.addToBattlefieldAndReturn(player, new DuskmantleHouseOfShadow());
        land.setSummoningSick(false);
        return land;
    }
}
