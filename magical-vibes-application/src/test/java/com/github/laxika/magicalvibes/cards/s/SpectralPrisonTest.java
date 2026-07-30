package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpectralPrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Spectral Prison attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpectralPrison()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spectral Prison")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        attachPrison(player1, creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Other creatures still untap normally")
    void otherCreaturesStillUntap() {
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        enchanted.tap();
        Permanent free = addCreatureReady(player2, new GrizzlyBears());
        free.tap();

        attachPrison(player1, enchanted);

        advanceToNextTurn(player1);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(free.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again once Spectral Prison leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent prison = attachPrison(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(prison);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Spectral Prison is sacrificed when the enchanted creature becomes the target of a spell")
    void sacrificedWhenEnchantedCreatureTargetedBySpell() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachPrison(player1, creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        // Shock plus Spectral Prison's triggered ability on top of it
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spectral Prison");
        harness.assertInGraveyard(player1, "Spectral Prison");
    }

    @Test
    @DisplayName("Spectral Prison is not sacrificed when the enchanted creature becomes the target of an ability")
    void notSacrificedWhenEnchantedCreatureTargetedByAbility() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachPrison(player1, creature);

        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent icy = findPermanent(player1, "Icy Manipulator");
        icy.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(icy), null, creature.getId());

        harness.passBothPriorities();

        // Only spells trigger the sacrifice — the Aura survives an ability targeting its host
        harness.assertOnBattlefield(player1, "Spectral Prison");
    }

    @Test
    @DisplayName("Targeting Spectral Prison itself does not trigger the sacrifice")
    void notTriggeredWhenPrisonItselfIsTargeted() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent prison = attachPrison(player1, creature);

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, prison.getId());

        // Naturalize alone — no sacrifice trigger stacked on top of it
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent attachPrison(Player controller, Permanent creature) {
        Permanent prison = new Permanent(new SpectralPrison());
        prison.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(prison);
        return prison;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
