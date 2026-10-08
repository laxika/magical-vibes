package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FalkenrathPerforator;
import com.github.laxika.magicalvibes.cards.p.PlayWithFire;
import com.github.laxika.magicalvibes.cards.s.StranglingGrasp;
import com.github.laxika.magicalvibes.cards.w.WrennAndSeven;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VengefulStrangler.class, FalkenrathPerforator.class, PlayWithFire.class, WrennAndSeven.class})
class VengefulStranglerTest extends BaseCardTest {

    @Test
    void returnsAttachedToOpponentPlaneswalker() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player1, new VengefulStrangler());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WrennAndSeven());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, strangler.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Strangling Grasp");
        assertThat(returned.getAttachedTo()).isEqualTo(target.getId());
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
    }

    @Test
    void staysInGraveyardWithoutLegalOpponentPermanent() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player1, new VengefulStrangler());
        addCreatureReady(player1, new FalkenrathPerforator());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, strangler.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Vengeful Strangler");
        harness.assertNotOnBattlefield(player1, "Strangling Grasp");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotBlock() {
        addCreatureReady(player2, new VengefulStrangler());
        Permanent attacker = addCreatureReady(player1, new FalkenrathPerforator());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotTriggerDuringEnchantedControllersUpkeep() {
        Permanent enchanted = addCreatureReady(player2, new FalkenrathPerforator());
        addGrasp(enchanted);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificingEnchantedPermanentStillLosesLife() {
        Permanent enchanted = addCreatureReady(player2, new FalkenrathPerforator());
        addCreatureReady(player2, new FalkenrathPerforator());
        addGrasp(enchanted);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(enchanted.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(enchanted);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Vengeful Strangler");
    }

    @Test
    void graspFallsOffWhenEnchantedCreatureChangesToAuraControllersControl() {
        Permanent enchanted = addCreatureReady(player2, new FalkenrathPerforator());
        Permanent grasp = addGrasp(enchanted);
        gd.playerBattlefields.get(player2.getId()).remove(enchanted);
        gd.playerBattlefields.get(player1.getId()).add(enchanted);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(grasp);
        harness.assertInGraveyard(player1, "Vengeful Strangler");
    }

    private Permanent addGrasp(Permanent enchanted) {
        Permanent grasp = harness.addToBattlefieldAndReturn(player1, new VengefulStrangler());
        grasp.setCard(grasp.getOriginalCard().getBackFaceCard());
        grasp.setTransformed(true);
        grasp.setAttachedTo(enchanted.getId());
        return grasp;
    }

    @Test
    void diesAndReturnsTransformedAttachedToTargetOpponentCreature() {
        Permanent strangler = harness.addToBattlefieldAndReturn(player1, new VengefulStrangler());
        Permanent target = addCreatureReady(player2, new FalkenrathPerforator());
        harness.setHand(player2, List.of(new PlayWithFire()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, strangler.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(strangler.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCard()).isInstanceOf(StranglingGrasp.class);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void enchantedControllerSacrificesNonlandPermanentThenLosesLifeAtUpkeep() {
        Permanent enchanted = addCreatureReady(player2, new FalkenrathPerforator());
        Permanent sacrifice = addCreatureReady(player2, new FalkenrathPerforator());
        addGrasp(enchanted);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(sacrifice.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchanted).doesNotContain(sacrifice);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }
}
