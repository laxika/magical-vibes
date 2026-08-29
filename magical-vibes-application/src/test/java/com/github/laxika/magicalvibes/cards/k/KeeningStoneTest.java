package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeeningStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills cards equal to the number of cards in their graveyard")
    void millsCardsEqualToTargetPlayersGraveyardSize() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new KeeningStone());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The mill count is evaluated when the ability resolves")
    void evaluatesGraveyardSizeAtResolution() {
        harness.addToBattlefield(player1, new KeeningStone());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerGraveyards.get(player2.getId()).add(new Forest());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The ability can target its controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new KeeningStone());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The ability cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new KeeningStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KeeningStone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
