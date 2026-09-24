package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieArtisans.class, PygmyRazorback.class})
class FaerieArtisansTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature an opponent controls creates an artifact token copy")
    void opponentNontokenCreatureCreatesArtifactCopy() {
        harness.addToBattlefield(player1, new FaerieArtisans());

        castPygmyRazorback(player2);

        Permanent token = findPermanents(player1, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    @DisplayName("A later nontoken creature replaces the previous token copy")
    void laterCreatureExilesPreviousCopy() {
        harness.addToBattlefield(player1, new FaerieArtisans());

        castPygmyRazorback(player2);
        Permanent firstToken = findPermanents(player1, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        castPygmyRazorback(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstToken);
        assertThat(findPermanents(player1, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    @DisplayName("Leaving Faerie Artisans exiles its current token copy")
    void leavingArtisansExilesCurrentCopy() {
        Permanent artisans = harness.addToBattlefieldAndReturn(player1, new FaerieArtisans());
        castPygmyRazorback(player2);
        Permanent token = findPermanents(player1, "Pygmy Razorback").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artisans));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    private void castPygmyRazorback(Player player) {
        harness.forceActivePlayer(player);
        harness.clearPriorityPassed();
        harness.setHand(player, java.util.List.of(new PygmyRazorback()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
