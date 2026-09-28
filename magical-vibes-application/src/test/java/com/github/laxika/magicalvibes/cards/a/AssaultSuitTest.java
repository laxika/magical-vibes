package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultSuit.class, GrizzlyBears.class})
class AssaultSuitTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostHasteAndSacrificeProtection() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachSuitTo(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.cantBeSacrificed(gd, creature)).isTrue();
    }

    @Test
    void opponentMayControlAndUntapEquippedCreatureUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();
        attachSuitTo(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player2.getId());
        assertThat(creature.isTapped()).isFalse();
        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));
        assertThat(gqs.findPermanentController(gd, creature.getId())).isEqualTo(player1.getId());
        assertThat(gqs.cantBeSacrificed(gd, creature)).isTrue();
    }

    private Permanent attachSuitTo(Permanent creature) {
        Permanent suit = harness.addToBattlefieldAndReturn(player1, new AssaultSuit());
        suit.setAttachedTo(creature.getId());
        return suit;
    }
}
