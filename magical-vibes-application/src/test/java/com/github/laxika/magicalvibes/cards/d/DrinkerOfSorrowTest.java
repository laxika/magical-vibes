package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EnormousBaloth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrinkerOfSorrow.class, DefiantElf.class, EnormousBaloth.class, Forest.class, FugitiveWizard.class})
class DrinkerOfSorrowTest extends BaseCardTest {

    @Test
    void combatDamageToPlayerMakesItsControllerSacrificeAPermanent() {
        Permanent drinker = addCreatureReady(player1, new DrinkerOfSorrow());
        drinker.setAttacking(true);
        Permanent sacrifice = addCreatureReady(player1, new Forest());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(sacrifice.getId()));

        harness.assertOnBattlefield(player1, "Drinker of Sorrow");
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void combatDamageToCreatureMakesItsControllerSacrificeAPermanent() {
        Permanent drinker = addCreatureReady(player1, new DrinkerOfSorrow());
        drinker.setAttacking(true);
        Permanent sacrifice = addCreatureReady(player1, new DefiantElf());

        Permanent blocker = addCreatureReady(player2, new FugitiveWizard());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(sacrifice.getId()));

        harness.assertOnBattlefield(player1, "Drinker of Sorrow");
        harness.assertNotOnBattlefield(player1, "Defiant Elf");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    void combatDamageTriggerStillResolvesIfDrinkerDiesInCombat() {
        Permanent drinker = addCreatureReady(player1, new DrinkerOfSorrow());
        drinker.setAttacking(true);
        Permanent sacrifice = addCreatureReady(player1, new DefiantElf());
        addCreatureReady(player1, new FugitiveWizard());

        Permanent blocker = addCreatureReady(player2, new EnormousBaloth());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(sacrifice.getId()));

        harness.assertInGraveyard(player1, "Drinker of Sorrow");
        harness.assertInGraveyard(player1, "Defiant Elf");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Enormous Baloth");
    }

    @Test
    void cannotBlock() {
        Permanent drinker = addCreatureReady(player1, new DrinkerOfSorrow());

        assertThat(bls.canBlock(gd, drinker)).isFalse();
    }
}
