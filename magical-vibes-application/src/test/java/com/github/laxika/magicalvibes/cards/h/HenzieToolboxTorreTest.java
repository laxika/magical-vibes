package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvyElemental;
import com.github.laxika.magicalvibes.cards.r.RiveteersRequisitioner;
import com.github.laxika.magicalvibes.cards.t.TorporOrb;
import com.github.laxika.magicalvibes.cards.w.WorkshopWarchief;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HenzieToolboxTorre.class, AirElemental.class, GrizzlyBears.class,
        IvyElemental.class, RiveteersRequisitioner.class, WorkshopWarchief.class, TorporOrb.class})
class HenzieToolboxTorreTest extends BaseCardTest {

    @Test
    @DisplayName("Grants blitz to creature spells with mana value 4 or greater")
    void grantsBlitzToLargeCreatureSpells() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent elemental = findPermanent(player1, "Air Elemental");
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Commander casts reduce the generic portion of blitz costs")
    void commanderCastCountReducesBlitzCost() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Air Elemental").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant blitz to creature spells with mana value less than 4")
    void doesNotGrantBlitzToSmallCreatureSpells() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesPrintedBlitzCostOfSmallCreature() {
        gd.recordCommanderCastFromCommandZone(player1.getId());
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new RiveteersRequisitioner()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Riveteers Requisitioner").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void canUseGrantedBlitzInsteadOfMoreExpensivePrintedBlitz() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Workshop Warchief").hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void chosenXCountsTowardBlitzEligibility() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCardWithAlternateCost(gd, player1, 0, 3, null, null, List.of());
        resolveAllTriggers();

        Permanent elemental = findPermanent(player1, "Ivy Elemental");
        assertThat(elemental.getPlusOnePlusOneCounters()).isEqualTo(3);
        assertThat(elemental.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void normalCastDoesNotGainBlitzBenefitsOrEndStepSacrifice() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Air Elemental").hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Air Elemental");
    }

    @Test
    void reductionCannotPayColoredMana() {
        for (int i = 0; i < 5; i++) {
            gd.recordCommanderCastFromCommandZone(player1.getId());
        }
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotGrantBlitzToOpponentsSpells() {
        harness.addToBattlefield(player2, new HenzieToolboxTorre());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void blitzHasteAndSacrificeDoNotDependOnEnterTriggers() {
        harness.addToBattlefield(player1, new HenzieToolboxTorre());
        harness.addToBattlefield(player1, new TorporOrb());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Air Elemental").hasKeyword(Keyword.HASTE)).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Air Elemental");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
