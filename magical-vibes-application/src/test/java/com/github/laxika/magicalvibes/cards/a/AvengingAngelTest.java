package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvengingAngelTest extends BaseCardTest {

    @Test
    void diesAndMayBePutOnTopOfItsOwnersLibrary() {
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new AvengingAngel());
        Permanent angel = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card angelCard = angel.getCard();

        destroyAngelWithWrath();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(angelCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(angelCard.getId()));
    }

    @Test
    void diesAndMayRemainInItsOwnersGraveyard() {
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new AvengingAngel());
        Permanent angel = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card angelCard = angel.getCard();

        destroyAngelWithWrath();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(angelCard.getId()));
    }

    private void destroyAngelWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
