package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(CelestialVault.class)
class CelestialVaultTest extends BaseCardTest {

    @Test
    void draftsCardFaceDownWithVault() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new CelestialVault());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.SpellbookDraftToExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftToExileChoice.class);
        assertThat(choice).isNotNull();
        Card drafted = choice.cards().getFirst();

        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        assertThat(gd.getCardsExiledByPermanent(vault.getId()))
                .anyMatch(card -> card.getId().equals(drafted.getId()));
        assertThat(gd.findExiledCard(drafted.getId()).faceDown()).isTrue();
    }

    @Test
    void sacrificingVaultReturnsItsExiledCardsToHand() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new CelestialVault());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.SpellbookDraftToExileChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftToExileChoice.class);
        Card drafted = choice.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(drafted.getId()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drafted);
        assertThat(gd.getCardsExiledByPermanent(vault.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(vault.getCard().getId()));
    }
}
