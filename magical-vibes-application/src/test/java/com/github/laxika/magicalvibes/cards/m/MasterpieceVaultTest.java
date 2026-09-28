package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChampionsHelm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningGreaves;
import com.github.laxika.magicalvibes.cards.s.SwordOfBodyAndMind;
import com.github.laxika.magicalvibes.cards.s.SwordOfFeastAndFamine;
import com.github.laxika.magicalvibes.cards.s.SwordOfFireAndIce;
import com.github.laxika.magicalvibes.cards.s.SwordOfLightAndShadow;
import com.github.laxika.magicalvibes.cards.s.SwordOfWarAndPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        MasterpieceVault.class,
        ChampionsHelm.class,
        LightningGreaves.class,
        SwordOfBodyAndMind.class,
        SwordOfFeastAndFamine.class,
        SwordOfFireAndIce.class,
        SwordOfLightAndShadow.class,
        SwordOfWarAndPeace.class,
        GrizzlyBears.class
})
class MasterpieceVaultTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing the Vault drafts an Equipment onto the battlefield and attaches it")
    void sacrificeDraftsAndAttachesEquipment() {
        Permanent vault = harness.addToBattlefieldAndReturn(player1, new MasterpieceVault());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card selected = chooseDraftedCard();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(selected.getId())
                        && permanent.getAttachedTo() != null
                        && permanent.getAttachedTo().equals(bears.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(vault.getCard().getId()));
    }

    @Test
    @DisplayName("The drafted Equipment attachment target can be declined")
    void canDeclineAttachmentTarget() {
        harness.addToBattlefield(player1, new MasterpieceVault());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card selected = chooseDraftedCard();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(selected.getId())
                        && permanent.getAttachedTo() == null);
    }

    private Card chooseDraftedCard() {
        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(3);
        Card selected = choice.allCards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        return selected;
    }
}
