package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({HeirloomBlade.class, GrizzlyBears.class, AirElemental.class, Forest.class, DoomBlade.class})
class HeirloomBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("May reveal until a matching creature is found and put it into hand")
    void findsCreatureSharingTypeWithDyingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        AirElemental elemental = new AirElemental();
        GrizzlyBears matchingCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, elemental, matchingCreature));

        killCreature(creature);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(forest.getId(), elemental.getId());
    }

    @Test
    @DisplayName("Declining the reveal leaves the library unchanged")
    void decliningLeavesLibraryUnchanged() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent blade = harness.addToBattlefieldAndReturn(player1, new HeirloomBlade());
        blade.setAttachedTo(creature.getId());
        Forest forest = new Forest();
        GrizzlyBears matchingCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(forest, matchingCreature));

        killCreature(creature);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, matchingCreature);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void killCreature(Permanent creature) {
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
    }
}
