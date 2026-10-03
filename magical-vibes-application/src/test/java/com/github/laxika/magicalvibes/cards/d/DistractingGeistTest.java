package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CleverDistraction;
import com.github.laxika.magicalvibes.cards.s.SelhoffEntomber;
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

@CardUsed({DistractingGeist.class, CleverDistraction.class, SelhoffEntomber.class})
class DistractingGeistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps a creature the defending player controls")
    void attackingTapsDefendingCreature() {
        addCreatureReady(player1, new DistractingGeist());
        Permanent victim = addCreatureReady(player2, new SelhoffEntomber());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Disturb enters transformed as Clever Distraction attached to a creature")
    void disturbEntersTransformedAttached() {
        Permanent creature = addCreatureReady(player1, new SelhoffEntomber());

        Permanent aura = castWithDisturb(creature);

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Clever Distraction grants the attack trigger to the enchanted creature")
    void cleverDistractionGrantsAttackTrigger() {
        Permanent creature = addCreatureReady(player1, new SelhoffEntomber());
        castWithDisturb(creature);
        Permanent victim = addCreatureReady(player2, new SelhoffEntomber());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Clever Distraction is exiled instead of going to the graveyard")
    void cleverDistractionIsExiledInsteadOfGraveyard() {
        Permanent creature = addCreatureReady(player1, new SelhoffEntomber());
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

    @Test
    void attackTriggerRejectsCreatureNotControlledByDefender() {
        Permanent geist = addCreatureReady(player1, new DistractingGeist());
        Permanent victim = addCreatureReady(player2, new SelhoffEntomber());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, geist.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void attackTriggerResolvesAfterGeistLeavesBattlefield() {
        Permanent geist = addCreatureReady(player1, new DistractingGeist());
        Permanent victim = addCreatureReady(player2, new SelhoffEntomber());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geist));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Distracting Geist");
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void grantedAttackTriggerRejectsAttackersOwnCreature() {
        Permanent creature = addCreatureReady(player1, new SelhoffEntomber());
        castWithDisturb(creature);
        Permanent otherCreature = addCreatureReady(player1, new SelhoffEntomber());
        Permanent victim = addCreatureReady(player2, new SelhoffEntomber());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void grantedAttackTriggerResolvesAfterAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player1, new SelhoffEntomber());
        Permanent aura = castWithDisturb(creature);
        Permanent victim = addCreatureReady(player2, new SelhoffEntomber());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void enchantedOpponentsCreatureControllerChoosesAttackTarget() {
        Permanent creature = addCreatureReady(player2, new SelhoffEntomber());
        castWithDisturb(creature);
        Permanent victim = addCreatureReady(player1, new SelhoffEntomber());

        declareAttackers(player2, List.of(0));
        harness.handlePermanentChosen(player2, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void grantedAttackTriggerUsesEnchantedCreatureAsSource() {
        Permanent creature = addCreatureReady(player1, new SelhoffEntomber());
        castWithDisturb(creature);
        Permanent victim = addCreatureReady(player2, new SelhoffEntomber());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, victim.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(creature.getId());
        harness.passBothPriorities();
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    void auraIsExiledWhenEnchantedCreatureDies() {
        Permanent creature = addCreatureReady(player2, new SelhoffEntomber());
        Permanent aura = castWithDisturb(creature);
        UUID cardId = aura.getOriginalCard().getId();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Selhoff Entomber");
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    @Test
    void disturbedSpellIsExiledWhenItsTargetDisappears() {
        Permanent creature = addCreatureReady(player2, new SelhoffEntomber());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        DistractingGeist geist = new DistractingGeist();
        harness.setGraveyard(player1, List.of(geist));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFlashback(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(geist.getId());
    }

    private Permanent castWithDisturb(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new DistractingGeist()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, creature.getId());

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }
}
