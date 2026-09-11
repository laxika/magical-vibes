package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.Extinction;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvengingAngel.class, Extinction.class, Plains.class})
class AvengingAngelTest extends BaseCardTest {

    @Test
    void diesAndMayBePutOnTopOfItsOwnersLibrary() {
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AvengingAngel());
        Card angelCard = angel.getCard();

        destroyAngelWithExtinction(player1);

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
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new AvengingAngel());
        Card angelCard = angel.getCard();

        destroyAngelWithExtinction(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(angelCard.getId()));
    }

    @Test
    void controlledByOpponentStillReturnsToOwnersLibrary() {
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        Card angelCard = new AvengingAngel();
        angelCard.setOwnerId(player1.getId());
        harness.addToBattlefieldAndReturn(player2, angelCard);

        destroyAngelWithExtinction(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(angelCard, topCard);
        assertThat(gd.playerDecks.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(angelCard.getId()));
    }

    private void destroyAngelWithExtinction(Player caster) {
        harness.castFromHand(caster, new Extinction(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(caster, "ANGEL");
        harness.passBothPriorities();
    }
}
