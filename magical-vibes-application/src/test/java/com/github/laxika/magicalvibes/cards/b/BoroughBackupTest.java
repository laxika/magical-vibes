package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoroughBackup.class, Forest.class, GrizzlyBears.class})
class BoroughBackupTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two 3/2 white Hero tokens with vigilance")
    void createsTwoHeroTokens() {
        harness.setHand(player1, List.of(new BoroughBackup()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Hero")).hasSize(2).allSatisfy(hero -> {
            assertThat(hero.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(hero.getCard().getSubtypes()).contains(CardSubtype.HERO);
            assertThat(hero.getEffectivePower()).isEqualTo(3);
            assertThat(hero.getEffectiveToughness()).isEqualTo(2);
            assertThat(hero.hasKeyword(Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    @DisplayName("Basic landcycling searches for a basic land and discards Borough Backup")
    void basicLandcyclingSearchesForBasicLand() {
        harness.setHand(player1, List.of(new BoroughBackup()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Borough Backup");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
