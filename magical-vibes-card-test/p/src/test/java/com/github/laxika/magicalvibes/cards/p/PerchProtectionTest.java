package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerchProtection.class, GrizzlyBears.class, LeylineOfSanctity.class})
class PerchProtectionTest extends BaseCardTest {

    @Test
    @DisplayName("Without the gift, creates four Birds and exiles itself")
    void withoutGiftCreatesBirdsAndExilesIt() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PerchProtection perchProtection = new PerchProtection();
        harness.setHand(player1, List.of(perchProtection));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithGift(player1, 0, null, List.of(), false);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Bird")).hasSize(4).allSatisfy(bird -> {
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(bird.getCard().getSubtypes()).containsExactly(CardSubtype.BIRD);
            assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, bird, Keyword.FLYING)).isTrue();
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.extraTurns).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(perchProtection.getId()));
    }

    @Test
    @DisplayName("A promised gift gives the opponent an extra turn and protects its controller")
    void promisedGiftGivesOpponentAnExtraTurnAndProtectsController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PerchProtection perchProtection = new PerchProtection();
        harness.setHand(player1, List.of(perchProtection));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithGift(player1, 0, null, List.of(), true);
        harness.passBothPriorities();

        List<Permanent> phasedOut = gd.phasedOutPermanents.get(player1.getId());
        assertThat(phasedOut).contains(bears);
        assertThat(phasedOut).extracting(p -> p.getCard().getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Bird", "Bird", "Bird", "Bird");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.extraTurns).containsExactly(player2.getId());
        assertThat(gqs.canPlayerLifeChange(gd, player1.getId())).isFalse();
        assertThat(gqs.playerHasProtectionFromEverything(gd, player1.getId())).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(perchProtection.getId()));
    }

    @Test
    @DisplayName("The gift can be promised to an opponent with hexproof, since the gift opponent isn't targeted")
    void giftCanBePromisedToHexproofOpponent() {
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setHand(player1, List.of(new PerchProtection()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithGift(player1, 0, null, List.of(), true);
        harness.passBothPriorities();

        assertThat(gd.extraTurns).containsExactly(player2.getId());
    }
}
