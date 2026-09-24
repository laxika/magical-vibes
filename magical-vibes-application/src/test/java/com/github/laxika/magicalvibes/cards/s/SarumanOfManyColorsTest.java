package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarumanOfManyColors.class, CounselOfTheSoratami.class, Divination.class,
        Forest.class, LightningBolt.class})
class SarumanOfManyColorsTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell mills each opponent and offers a qualifying graveyard copy")
    void secondSpellMillsAndCopiesEligibleSpell() {
        SarumanOfManyColors saruman = new SarumanOfManyColors();
        Divination graveyardTarget = new Divination();
        harness.addToBattlefield(player1, saruman);
        harness.setGraveyard(player2, List.of(graveyardTarget));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new CounselOfTheSoratami(), new CounselOfTheSoratami()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validCardIds()).containsExactly(graveyardTarget.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardTarget.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardTarget.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Ward only permits discarding an enchantment, instant, or sorcery")
    void wardFiltersDiscardChoice() {
        SarumanOfManyColors saruman = new SarumanOfManyColors();
        Forest land = new Forest();
        Divination validDiscard = new Divination();
        var sarumanPermanent = harness.addToBattlefieldAndReturn(player1, saruman);
        harness.setHand(player2, List.of(new LightningBolt(), land, validDiscard));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, sarumanPermanent.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player2, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(saruman.getId()));
    }
}
