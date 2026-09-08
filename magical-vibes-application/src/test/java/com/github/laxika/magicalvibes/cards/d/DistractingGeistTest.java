package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CleverDistraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({DistractingGeist.class, CleverDistraction.class, GrizzlyBears.class})
class DistractingGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps a creature the defending player controls")
    void attackingTapsDefendingCreature() {
        addCreatureReady(player1, new DistractingGeist());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Disturb enters transformed as Clever Distraction attached to a creature")
    void disturbEntersTransformedAttached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = castWithDisturb(creature);

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Clever Distraction grants the attack trigger to the enchanted creature")
    void cleverDistractionGrantsAttackTrigger() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castWithDisturb(creature);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Clever Distraction is exiled instead of going to the graveyard")
    void cleverDistractionIsExiledInsteadOfGraveyard() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castWithDisturb(creature);
        UUID auraCardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(auraCardId);
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DistractingGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    private Permanent castWithDisturb(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DistractingGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }
}
