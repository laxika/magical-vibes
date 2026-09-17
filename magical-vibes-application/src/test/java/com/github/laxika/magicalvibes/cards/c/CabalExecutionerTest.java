package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalExecutioner.class, ElvishWarrior.class, Forest.class})
class CabalExecutionerTest extends BaseCardTest {

    @Test
    void damagedPlayerChoosesACreatureToSacrifice() {
        Permanent executioner = addCreatureReady(player1, new CabalExecutioner());
        executioner.setAttacking(true);
        Permanent ownCreature = addCreatureReady(player1, new ElvishWarrior());
        Permanent enemyCreature = addCreatureReady(player2, new ElvishWarrior());
        Permanent secondEnemyCreature = addCreatureReady(player2, new ElvishWarrior());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(enemyCreature.getId(), secondEnemyCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player2, enemyCreature.getId());

        harness.assertInGraveyard(player2, "Elvish Warrior");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(secondEnemyCreature.getId())
                .doesNotContain(enemyCreature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(ownCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    void blockedExecutionerDoesNotTrigger() {
        Permanent executioner = addCreatureReady(player1, new CabalExecutioner());
        executioner.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void onlyCreaturesAreSacrificeChoices() {
        Permanent executioner = addCreatureReady(player1, new CabalExecutioner());
        executioner.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new ElvishWarrior());
        Permanent secondEnemyCreature = addCreatureReady(player2, new ElvishWarrior());
        Permanent enemyLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(enemyCreature.getId(), secondEnemyCreature.getId())
                .doesNotContain(enemyLand.getId());

        harness.handlePermanentChosen(player2, enemyCreature.getId());

        harness.assertInGraveyard(player2, "Elvish Warrior");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(secondEnemyCreature.getId())
                .doesNotContain(enemyCreature.getId());
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    void noSacrificeWhenDamagedPlayerControlsNoCreatures() {
        Permanent executioner = addCreatureReady(player1, new CabalExecutioner());
        executioner.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new CabalExecutioner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent executioner = findPermanent(player1, "Cabal Executioner");
        assertThat(executioner.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(executioner));
        harness.passBothPriorities();

        assertThat(executioner.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }
}
