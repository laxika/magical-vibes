package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DaggerclawImp;
import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlindHunter.class, DaggerclawImp.class, DouseInGloom.class, GruulSignet.class})
class BlindHunterTest extends BaseCardTest {

    @Test
    void drainsOnEnteringAndWhenTheHauntedCreatureDies() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());

        castHunterAndChooseTarget(player2.getId());

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);

        UUID hunterId = harness.getPermanentId(player1, "Blind Hunter");
        destroyWithDouseInGloom(player1, hunterId);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Blind Hunter"));

        destroyWithDouseInGloom(player2, hauntedCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 18);
    }

    @Test
    void enteringAbilityCanTargetItsController() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        castHunterAndChooseTarget(player1.getId());

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    void hauntedCreatureDeathResolvesLifeLossAndGainTogether() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        Permanent hauntedCreature = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        castHunterAndChooseTarget(player2.getId());

        destroyWithDouseInGloom(player1, harness.getPermanentId(player1, "Blind Hunter"));
        harness.handlePermanentChosen(player1, hauntedCreature.getId());
        harness.passBothPriorities();

        destroyWithDouseInGloom(player2, hauntedCreature.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hauntOnlyOffersCreatureTargets() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new GruulSignet());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DaggerclawImp());
        castHunterAndChooseTarget(player2.getId());

        UUID hunterId = harness.getPermanentId(player1, "Blind Hunter");
        destroyWithDouseInGloom(player1, hunterId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(creature.getId())
                .doesNotContain(nonCreature.getId());
    }

    @Test
    void doesNotHauntWhenNoCreatureIsAvailable() {
        castHunterAndChooseTarget(player2.getId());

        UUID hunterId = harness.getPermanentId(player1, "Blind Hunter");
        destroyWithDouseInGloom(player1, hunterId);

        harness.assertInGraveyard(player1, "Blind Hunter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Blind Hunter"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castHunterAndChooseTarget(UUID targetPlayerId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new BlindHunter(), "{2}{W}{B}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetPlayerId);
        harness.passBothPriorities();
    }

    private void destroyWithDouseInGloom(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DouseInGloom()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
