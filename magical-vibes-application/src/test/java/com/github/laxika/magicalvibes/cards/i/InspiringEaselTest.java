package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiringEasel.class, GrizzlyBears.class, LightningBolt.class})
class InspiringEaselTest extends BaseCardTest {

    @Test
    void producesAnyColorManaRestrictedToInstantAndSorcerySpells() {
        Permanent easel = harness.addToBattlefieldAndReturn(player1, new InspiringEasel());
        easel.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void selectedSpellPerpetuallyIncorporatesCostAndCopiesWhenCast() {
        Permanent easel = harness.addToBattlefieldAndReturn(player1, new InspiringEasel());
        easel.setSummoningSick(false);
        LightningBolt chosen = new LightningBolt();
        harness.setHand(player1, List.of(new GrizzlyBears(), chosen));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        ManaCost incorporated = gd.perpetualManaCostIncreases.get(chosen.getId());
        assertThat(incorporated.countColorSymbols(ManaColor.BLUE)).isEqualTo(1);
        assertThat(incorporated.countColorSymbols(ManaColor.RED)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 1, player2.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getDescription() != null
                && entry.getDescription().startsWith("Copy of Lightning Bolt"));
    }

    @Test
    void restrictedManaCanPayForAnInstant() {
        harness.addToBattlefield(player1, new InspiringEasel());
        harness.setHand(player1, List.of(new LightningBolt()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOnlyColored(ManaColor.RED))
                .isZero();
    }

    @Test
    void incorporationCannotBeActivatedOutsideYourMainPhase() {
        Permanent easel = harness.addToBattlefieldAndReturn(player1, new InspiringEasel());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(easel.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void incorporationResolvesWithoutAChoiceWhenThereAreNoEligibleCards() {
        Permanent easel = harness.addToBattlefieldAndReturn(player1, new InspiringEasel());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(easel.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void repeatedIncorporationGrantsIndependentCopyTriggers() {
        harness.addToBattlefield(player1, new InspiringEasel());
        harness.addToBattlefield(player1, new InspiringEasel());
        harness.setHand(player1, List.of(new LightningBolt()));

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, i, 1, null, null);
            harness.passBothPriorities();
            harness.handleCardChosen(player1, 0);
        }

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());

        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2);
        resolveAllTriggers();
        harness.assertLife(player2, 11);
    }

    @Test
    void incorporatedSpellCopyCanChooseANewTarget() {
        harness.addToBattlefield(player1, new InspiringEasel());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
    }
}
