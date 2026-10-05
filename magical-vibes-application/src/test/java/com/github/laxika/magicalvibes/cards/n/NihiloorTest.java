package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nihiloor.class, BalefulStrix.class, SwordsToPlowshares.class})
class NihiloorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps your creature and steals an opponent creature within its power")
    void entersTapsAndSteals() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();

        PendingInteraction.MultiPermanentChoice tapChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(tapChoice).isNotNull();
        assertThat(tapChoice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(tapper.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
    }

    @Test
    @DisplayName("Attacking with an opponent-owned creature drains its owner")
    void attackingOpponentOwnedCreatureDrainsItsOwner() {
        addCreatureReady(player1, new Nihiloor());
        Permanent stolenAttacker = addCreatureReady(player1, new BalefulStrix());
        gd.stolenCreatures.put(stolenAttacker.getId(), player2.getId());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void mayDeclineToTap() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();

        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Nihiloor").isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTapNihiloorDespiteSummoningSickness() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();
        Permanent nihiloor = findPermanent(player1, "Nihiloor");

        harness.handleMultiplePermanentsChosen(player1, List.of(nihiloor.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();

        assertThat(nihiloor.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
    }

    @Test
    void tappedCreaturesCannotBeChosen() {
        Permanent tappedCreature = addCreatureReady(player1, new BalefulStrix());
        tappedCreature.setTapped(true);
        castNihiloor();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).doesNotContain(tappedCreature.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();
    }

    @Test
    void targetBecomesIllegalWhenTappedCreaturesPowerDrops() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        opponentCreature.setPowerModifier(1);
        castNihiloor();
        Permanent nihiloor = findPermanent(player1, "Nihiloor");
        harness.handleMultiplePermanentsChosen(player1, List.of(nihiloor.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        nihiloor.setPowerModifier(-2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentCreature);
    }

    @Test
    void targetBecomesIllegalWhenItsPowerExceedsTappedCreaturesPower() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        opponentCreature.setPowerModifier(1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void theftUsesTappedCreaturesIncreasedPowerAtResolution() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        tapper.setPowerModifier(1);
        opponentCreature.setPowerModifier(1);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
    }

    @Test
    void untappingTappedCreatureDoesNotPreventTheft() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        tapper.setTapped(false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
    }

    @Test
    void theftUsesTappedCreaturesLastPowerWhenItLeaves() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        tapper.setPowerModifier(2);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        opponentCreature.setPowerModifier(1);
        castNihiloor();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        tapper.setPowerModifier(0);
        exileCreature(tapper);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void removingNihiloorInResponsePreventsTheft() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        exileCreature(findPermanent(player1, "Nihiloor"));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void removingNihiloorReturnsStolenCreature() {
        Permanent tapper = addCreatureReady(player1, new BalefulStrix());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BalefulStrix());
        castNihiloor();
        harness.handleMultiplePermanentsChosen(player1, List.of(tapper.getId()));
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);

        exileCreature(findPermanent(player1, "Nihiloor"));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void eachOpponentOwnedAttackerTriggersSeparately() {
        addCreatureReady(player1, new Nihiloor());
        Permanent first = addCreatureReady(player1, new BalefulStrix());
        Permanent second = addCreatureReady(player1, new BalefulStrix());
        gd.stolenCreatures.put(first.getId(), player2.getId());
        gd.stolenCreatures.put(second.getId(), player2.getId());

        declareAttackers(List.of(1, 2));
        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void attackingWithOwnCreatureDoesNotTriggerDrain() {
        addCreatureReady(player1, new Nihiloor());
        addCreatureReady(player1, new BalefulStrix());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void exileCreature(Permanent creature) {
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }

    private void castNihiloor() {
        harness.setHand(player1, List.of(new Nihiloor()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
