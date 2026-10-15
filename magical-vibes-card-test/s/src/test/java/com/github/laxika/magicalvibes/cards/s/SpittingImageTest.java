package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpittingImage.class, GrizzlyBears.class, Mountain.class})
class SpittingImageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of target creature")
    void createsTokenCopyOfCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpittingImage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpittingImage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        // Token enters under the caster (player1), not the original's controller.
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new SpittingImage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player1, "Mountain");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Retrace recasts from the graveyard by discarding a land")
    void retraceRecastsFromGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SpittingImage()));
        harness.setHand(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castRetrace(player1, 0, 0, targetId);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertInGraveyard(player1, "Spitting Image");
    }

    @Test
    @DisplayName("Copy does not inherit counters, tapped status, or marked damage")
    void copyDoesNotInheritPermanentState() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var original = gd.playerBattlefields.get(player1.getId()).getFirst();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.tap();
        original.setMarkedDamage(1);
        harness.setHand(player1, List.of(new SpittingImage()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, original.getId());

        var tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getPlusOnePlusOneCounters()).isZero();
        assertThat(tokens.getFirst().isTapped()).isFalse();
        assertThat(tokens.getFirst().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Retrace rejects discarding a nonland card")
    void retraceRequiresLandDiscard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SpittingImage()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Spitting Image");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Retrace still requires the spell's mana cost")
    void retraceRequiresMana() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SpittingImage()));
        harness.setHand(player1, List.of(new Mountain()));
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Mountain");
        harness.assertInGraveyard(player1, "Spitting Image");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same card can be retraced repeatedly and can copy a token")
    void retraceAgainCopyingToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new SpittingImage()));
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castRetrace(player1, 0, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        var firstToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int spellIndex = java.util.stream.IntStream.range(0, gd.playerGraveyards.get(player1.getId()).size())
                .filter(i -> gd.playerGraveyards.get(player1.getId()).get(i).getName().equals("Spitting Image"))
                .findFirst().orElseThrow();
        harness.castRetrace(player1, spellIndex, 0, firstToken.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Grizzly Bears")))
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Spitting Image");
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Mountain"))).hasSize(2);
    }
}
