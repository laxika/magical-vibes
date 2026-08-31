package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LocthwainScorn;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VirtueOfPersistence.class, LocthwainScorn.class, GrizzlyBears.class, Island.class})
class VirtueOfPersistenceTest extends BaseCardTest {

    @Test
    void adventureShrinksCreatureGainsLifeAndExilesCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        VirtueOfPersistence card = new VirtueOfPersistence();
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        VirtueOfPersistence card = new VirtueOfPersistence();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentFaceReturnsTargetCreatureFromAnyGraveyard() {
        VirtueOfPersistence card = new VirtueOfPersistence();
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, card);
        harness.setGraveyard(player2, List.of(creature));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(cardInGraveyard -> cardInGraveyard.getId().equals(creature.getId()));
    }

    @Test
    void enchantmentFaceDoesNotTriggerWithoutCreatureCardInAnyGraveyard() {
        harness.addToBattlefield(player1, new VirtueOfPersistence());
        harness.setGraveyard(player2, List.of(new Island()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enchantmentFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        VirtueOfPersistence card = new VirtueOfPersistence();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
