package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonsilverKey.class, Forest.class, MindStone.class, FountainOfYouth.class, GrizzlyBears.class})
class MoonsilverKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a mana-producing artifact or basic land")
    void searchesForManaProducingArtifactOrBasicLand() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        setLibrary(new FountainOfYouth(), new Forest(), new MindStone(), new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card instanceof Forest || card instanceof MindStone)
                .anyMatch(card -> card instanceof Forest)
                .anyMatch(card -> card instanceof MindStone)
                .noneMatch(card -> card instanceof FountainOfYouth || card instanceof GrizzlyBears);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest || card instanceof MindStone);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MoonsilverKey);
    }

    @Test
    @DisplayName("Does not offer artifacts without mana abilities")
    void doesNotOfferArtifactsWithoutManaAbilities() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        setLibrary(new FountainOfYouth(), new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof FountainOfYouth || card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MoonsilverKey);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
