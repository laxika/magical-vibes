package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.k.KrenkosCommand;
import com.github.laxika.magicalvibes.cards.p.PygmyRazorback;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({FaerieArtisans.class, PygmyRazorback.class, KrenkosCommand.class})
class FaerieArtisansTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an opponent's nontoken creature as an artifact")
    void copiesOpponentNontokenCreatureAsArtifact() {
        harness.addToBattlefield(player1, new FaerieArtisans());

        castPygmyRazorback(player2);

        assertThat(findPermanents(player1, "Pygmy Razorback")).singleElement()
                .satisfies(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
                });
        assertThat(findPermanents(player2, "Pygmy Razorback")).singleElement()
                .satisfies(creature -> assertThat(creature.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("Exiles the previous token copy when another opponent creature enters")
    void exilesPreviousCopy() {
        harness.addToBattlefield(player1, new FaerieArtisans());

        castPygmyRazorback(player2);
        Permanent firstToken = findPermanents(player1, "Pygmy Razorback").getFirst();
        castPygmyRazorback(player2);

        assertThat(findPermanents(player1, "Pygmy Razorback")).singleElement()
                .satisfies(token -> assertThat(token.getId()).isNotEqualTo(firstToken.getId()));
    }

    @Test
    @DisplayName("Does not trigger for a token creature entering under an opponent's control")
    void ignoresTokenCreatures() {
        harness.addToBattlefield(player1, new FaerieArtisans());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new KrenkosCommand()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Goblin")).hasSize(2);
        assertThat(findPermanents(player1, "Goblin")).isEmpty();
    }

    private void castPygmyRazorback(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new PygmyRazorback()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);

        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
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

}
