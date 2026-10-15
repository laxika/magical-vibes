package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SparkReaper.class, JayaVeneratedFiremage.class, Forest.class, PrimordialWurm.class})
class SparkReaperTest extends BaseCardTest {

    @Test
    void sacrificesCreatureGainsLifeAndDrawsCard() {
        Permanent reaper = addCreatureReady(player1, new SparkReaper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(reaper), null, null);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertInGraveyard(player1, "Primordial Wurm");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canSacrificePlaneswalkerAndSourceIsAValidChoice() {
        Permanent reaper = addCreatureReady(player1, new SparkReaper());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JayaVeneratedFiremage());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(reaper), null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(reaper.getId(), planeswalker.getId());

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jaya, Venerated Firemage");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canSacrificeTappedSummoningSickSourceAndResolveAfterItLeaves() {
        Permanent reaper = harness.addToBattlefieldAndReturn(player1, new SparkReaper());
        reaper.setSummoningSick(true);
        reaper.tap();
        harness.addToBattlefield(player1, new PrimordialWurm());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, reaper.getId());

        harness.assertInGraveyard(player1, "Spark Reaper");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void sacrificeChoicesExcludeLandsAndOpponentsPermanents() {
        Permanent reaper = addCreatureReady(player1, new SparkReaper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new JayaVeneratedFiremage());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new PrimordialWurm());
        harness.addToBattlefield(player2, new JayaVeneratedFiremage());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                reaper.getId(), creature.getId(), planeswalker.getId());
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canActivateAgainWithoutTapping() {
        Permanent reaper = addCreatureReady(player1, new SparkReaper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.addToBattlefield(player1, new PrimordialWurm());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        assertThat(reaper.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, reaper.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Spark Reaper");
        harness.assertInGraveyard(player1, "Primordial Wurm");
    }

}
