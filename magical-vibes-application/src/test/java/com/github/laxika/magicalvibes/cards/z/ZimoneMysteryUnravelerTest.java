package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZimoneMysteryUnraveler.class, Forest.class, GrizzlyBears.class})
class ZimoneMysteryUnravelerTest extends BaseCardTest {

    @Test
    void firstLandfallManifestsDread() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
    }

    @Test
    void laterLandfallMayTurnAControlledPermanentFaceUp() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.addToBattlefield(player1, new ZimoneMysteryUnraveler());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        Permanent manifestedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(manifestedCard.getId()))
                .findFirst()
                .orElseThrow();
        gd.landsPlayedThisTurn.put(player1.getId(), 0);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(manifestedPermanent.isFaceDown()).isFalse();
    }
}
