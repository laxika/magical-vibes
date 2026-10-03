package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneArtisan.class, Forest.class, GrizzlyBears.class})
class ArcaneArtisanTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws, exiles a creature card, and creates a token copy under their control")
    void targetPlayerDrawsAndCreatesCreatureCopy() {
        Permanent artisan = addCreatureReady(player1, new ArcaneArtisan());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ExileCardFromHandAndCreateTokenCopyChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).noneMatch(card -> card instanceof GrizzlyBears);
        Permanent token = findPermanents(player2, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artisan));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
    }

    @Test
    @DisplayName("Exiling a noncreature card creates no token")
    void exilingNoncreatureCreatesNoToken() {
        addCreatureReady(player1, new ArcaneArtisan());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
