package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmergentWoodwurm.class, Forest.class, GrizzlyBears.class, Shock.class})
class EmergentWoodwurmTest extends BaseCardTest {

    @Test
    @DisplayName("Backup grants another creature the power-scaled attack trigger")
    void backupGrantsAttackTriggerToAnotherCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(forest, bears, shock));

        castWoodwurmTargeting(attacker);

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        declareAttacker(attacker);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(forest, bears, shock);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(bears, shock);
    }

    @Test
    @DisplayName("Backup targeting Emergent Woodwurm itself does not add a second attack trigger")
    void backupTargetingItselfDoesNotDuplicateAttackTrigger() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        Permanent woodwurm = castWoodwurmTargetingItself();

        assertThat(woodwurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        woodwurm.setSummoningSick(false);
        declareAttacker(woodwurm);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Backup's granted attack trigger expires at the end of the turn")
    void grantedAttackTriggerExpiresAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castWoodwurmTargeting(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttacker(attacker);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    private Permanent castWoodwurmTargeting(Permanent target) {
        harness.setHand(player1, List.of(new EmergentWoodwurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Emergent Woodwurm");
    }

    private Permanent castWoodwurmTargetingItself() {
        harness.setHand(player1, List.of(new EmergentWoodwurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent woodwurm = findPermanent(player1, "Emergent Woodwurm");
        harness.handlePermanentChosen(player1, woodwurm.getId());
        harness.passBothPriorities();
        return woodwurm;
    }

    private void declareAttacker(Permanent attacker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
    }
}
