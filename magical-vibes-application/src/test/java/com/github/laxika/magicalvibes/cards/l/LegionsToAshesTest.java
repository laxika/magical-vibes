package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LegionsToAshes.class, GrizzlyBears.class, Forest.class})
class LegionsToAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the target and same-name tokens controlled by its controller")
    void exilesTargetAndMatchingTokensOnly() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, token("Grizzly Bears"));
        harness.addToBattlefield(player2, token("Grizzly Bears"));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, token("Saproling"));
        harness.addToBattlefield(player1, token("Grizzly Bears"));
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        cast(targetId);

        assertThat(findPermanents(player2, "Grizzly Bears"))
                .hasSize(1)
                .allMatch(permanent -> !permanent.getCard().isToken());
        assertThat(findPermanents(player2, "Saproling")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        UUID landId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> cast(landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownPermanentId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> cast(ownPermanentId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent an opponent controls");
    }

    @Test
    void canTargetTokenAndExilesMatchingLandTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, token("Grizzly Bears"));
        Card landToken = token("Grizzly Bears");
        landToken.setType(CardType.LAND);
        harness.addToBattlefield(player2, landToken);
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(target.getId());

        assertThat(findPermanents(player2, "Grizzly Bears"))
                .hasSize(1)
                .allMatch(permanent -> !permanent.getCard().isToken());
    }

    @Test
    void missingTargetDoesNotExileMatchingTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent matchingToken = harness.addToBattlefieldAndReturn(player2, token("Grizzly Bears"));
        prepareSpell();
        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(matchingToken);
        harness.assertInGraveyard(player1, "Legions to Ashes");
    }

    @Test
    void faceDownTargetDoesNotMatchItsPrintedName() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent matchingPrintedName = harness.addToBattlefieldAndReturn(player2, token("Grizzly Bears"));

        cast(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(target)
                .contains(matchingPrintedName);
    }

    @Test
    void faceDownTokenDoesNotMatchItsPrintedName() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent faceDownToken = harness.addToBattlefieldAndReturn(player2, token("Grizzly Bears"));
        faceDownToken.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        cast(target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(target)
                .contains(faceDownToken);
    }

    private void cast(UUID targetId) {
        prepareSpell();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void prepareSpell() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LegionsToAshes()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Card token(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
