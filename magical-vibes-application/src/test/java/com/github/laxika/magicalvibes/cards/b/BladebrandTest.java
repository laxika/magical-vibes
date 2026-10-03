package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Bladebrand.class, GrizzlyBears.class, FountainOfYouth.class})
class BladebrandTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving grants deathtouch to the target creature")
    void resolvingGrantsDeathtouch() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bladebrand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Resolving draws a card")
    void resolvingDrawsACard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bladebrand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bladebrand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.hasKeyword(Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Bladebrand()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targeting an opponent's creature draws for the caster")
    void opponentCreatureDrawsForCaster() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bladebrand()));
        harness.setHand(player2, List.of());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bladebrand");
    }

    @Test
    @DisplayName("No card is drawn when the only target leaves the battlefield")
    void missingTargetPreventsDraw() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Bladebrand()));
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        harness.assertInGraveyard(player1, "Bladebrand");
    }
}
