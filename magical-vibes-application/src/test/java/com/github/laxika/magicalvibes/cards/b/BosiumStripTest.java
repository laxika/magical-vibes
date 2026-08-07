package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BosiumStripTest extends BaseCardTest {

    private void addReadyStrip() {
        harness.addToBattlefield(player1, new BosiumStrip());
    }

    @Test
    @DisplayName("After activation, top instant of graveyard can be cast and is exiled")
    void castsTopInstantAndExiles() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addReadyStrip();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Only the top graveyard card is castable; buried instant is not")
    void onlyTopCardIsCastable() {
        Shock buried = new Shock();
        LightningBolt top = new LightningBolt();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addReadyStrip();
        harness.setGraveyard(player1, List.of(buried, top));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");

        harness.castFlashback(player1, 1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Lightning Bolt");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("After casting the top spell, the new top instant may also be cast")
    void canCastNextTopAfterFirst() {
        Shock first = new Shock();
        LightningBolt second = new LightningBolt();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent creature2 = addCreatureReady(player2, new GrizzlyBears());
        addReadyStrip();
        // first is buried, second is top; cast second then first becomes top
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 1, creature.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFlashback(player1, 0, creature2.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(creature2.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(c -> c.getName())
                .containsExactlyInAnyOrder("Lightning Bolt", "Shock");
    }

    @Test
    @DisplayName("Top creature card cannot be cast via the permission")
    void topCreatureCannotBeCast() {
        GrizzlyBears bears = new GrizzlyBears();
        addReadyStrip();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("Without activating, cannot cast from the top of the graveyard")
    void cannotCastWithoutActivation() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addReadyStrip();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }

    @Test
    @DisplayName("Permission wears off at end of turn")
    void permissionEndsAtEndOfTurn() {
        Shock shock = new Shock();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addReadyStrip();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).contains(player1.getId());

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gd.mayCastTopInstantOrSorceryFromGraveyardUntilEndOfTurn).doesNotContain(player1.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }
}
