package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticCruiser.class, GrizzlyBears.class, PathToExile.class})
class CrypticCruiserTest extends BaseCardTest {

    @Test
    void putsOpponentOwnedExiledCardIntoItsOwnersGraveyardAndTapsTargetCreature() {
        Permanent cruiser = harness.addToBattlefieldAndReturn(player1, new CrypticCruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile exiledCard = new PathToExile();
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Path to Exile");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(target.isTapped()).isTrue();
        assertThat(cruiser.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotAllowActivatingWithoutOpponentOwnedExiledCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrypticCruiser());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void cannotUseControllerOwnedExiledCardsAsCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new CrypticCruiser());
        harness.setExile(player1, List.of(new PathToExile()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void canChooseAmongMultipleOpponentOwnedExiledCards() {
        harness.addToBattlefield(player1, new CrypticCruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        PathToExile first = new PathToExile();
        PathToExile second = new PathToExile();
        harness.setExile(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        harness.assertInGraveyard(player2, "Path to Exile");
        assertThat(target.isTapped()).isTrue();
    }
}
