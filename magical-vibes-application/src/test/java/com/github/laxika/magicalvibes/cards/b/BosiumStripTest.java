package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FitOfRage;
import com.github.laxika.magicalvibes.cards.s.StripedBears;
import com.github.laxika.magicalvibes.cards.v.Vitalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BosiumStrip.class, FitOfRage.class, StripedBears.class, Vitalize.class})
class BosiumStripTest extends BaseCardTest {

    private void addReadyStrip() {
        harness.addToBattlefield(player1, new BosiumStrip());
    }

    @Test
    @DisplayName("After activation, top instant of graveyard can be cast and is exiled")
    void castsTopInstantAndExiles() {
        addReadyStrip();
        Permanent creature = addCreatureReady(player1, new StripedBears());
        creature.tap();
        Vitalize vitalize = new Vitalize();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Vitalize");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vitalize"));
    }

    @Test
    @DisplayName("Only the top graveyard card is castable; buried instant is not")
    void onlyTopCardIsCastable() {
        Vitalize buried = new Vitalize();
        Vitalize top = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(buried, top));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        harness.castFromGraveyard(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(buried);
    }

    @Test
    @DisplayName("After casting the top spell, the new top instant may also be cast")
    void canCastNextTopAfterFirst() {
        Vitalize first = new Vitalize();
        Vitalize second = new Vitalize();
        addReadyStrip();
        // first is buried, second is top; cast second then first becomes top
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player1, 1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Vitalize", "Vitalize");
    }

    @Test
    @DisplayName("Top creature card cannot be cast via the permission")
    void topCreatureCannotBeCast() {
        StripedBears bears = new StripedBears();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 4);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("Without activating, cannot cast from the top of the graveyard")
    void cannotCastWithoutActivation() {
        Vitalize vitalize = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("A sorcery on top of the graveyard can also be cast for its normal cost")
    void castsTopSorcery() {
        FitOfRage fitOfRage = new FitOfRage();
        addReadyStrip();
        Permanent creature = addCreatureReady(player1, new StripedBears());
        harness.setGraveyard(player1, List.of(fitOfRage));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromGraveyardTargeting(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Fit of Rage");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fit of Rage"));
    }

    @Test
    @DisplayName("Permission wears off at end of turn")
    void permissionEndsAtEndOfTurn() {
        Vitalize vitalize = new Vitalize();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(vitalize));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).contains(player1.getId());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).doesNotContain(player1.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }
}
