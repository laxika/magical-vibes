package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LiegeOfThePit.class, AshcoatBear.class, HavenwoodWurm.class})
class LiegeOfThePitTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new LiegeOfThePit()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent liege = findPermanent(player1, "Liege of the Pit");
        assertThat(liege.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(liege));
        harness.passBothPriorities();

        assertThat(liege.isFaceDown()).isFalse();
    }

    @Test
    void faceDownMorphDoesNotTriggerItsUpkeepAbility() {
        harness.setHand(player1, List.of(new LiegeOfThePit()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent liege = findPermanent(player1, "Liege of the Pit");
        assertThat(liege.isFaceDown()).isTrue();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(liege.isFaceDown()).isTrue();
    }

    @Test
    void dealsDamageWhenNoOtherCreatureIsAvailable() {
        harness.addToBattlefield(player1, new LiegeOfThePit());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 7);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new LiegeOfThePit());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void sacrificesAnotherCreatureInsteadOfDealingDamage() {
        harness.addToBattlefield(player1, new LiegeOfThePit());
        harness.addToBattlefield(player1, new AshcoatBear());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void sacrificesTheChosenCreatureWhenMultipleAreAvailable() {
        harness.addToBattlefield(player1, new LiegeOfThePit());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new HavenwoodWurm());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wurm.getId());

        harness.assertOnBattlefield(player1, "Liege of the Pit");
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player1, "Havenwood Wurm");
        harness.assertInGraveyard(player1, "Havenwood Wurm");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void excludesLiegeFromTheSacrificeChoices() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new LiegeOfThePit());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new HavenwoodWurm());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(bear.getId(), wurm.getId())
                .doesNotContain(liege.getId());
    }

    @Test
    void opposingCreatureCannotBeSacrificedForTheUpkeepAbility() {
        harness.addToBattlefield(player1, new LiegeOfThePit());
        harness.addToBattlefield(player2, new AshcoatBear());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Liege of the Pit");
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
        harness.assertLife(player1, lifeBefore - 7);
    }

    @Test
    void anotherLiegeCanBeSacrificedAndItsPendingTriggerStillResolves() {
        harness.addToBattlefield(player1, new LiegeOfThePit());
        harness.addToBattlefield(player1, new LiegeOfThePit());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Liege of the Pit")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Liege of the Pit");
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Liege of the Pit");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore);
    }
}
