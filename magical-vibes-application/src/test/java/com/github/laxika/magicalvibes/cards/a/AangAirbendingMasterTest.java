package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({AangAirbendingMaster.class, GrizzlyBears.class, DressDown.class})
class AangAirbendingMasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB airbends another creature")
    void airbendsAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AangAirbendingMaster()));
        addAangMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(target.getOriginalCard().getId())).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Gets one experience counter when one or more controlled creatures leave without dying")
    void gainsOneExperienceForMultipleControlledCreaturesLeavingWithoutDying() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().beginPermanentLeaveBatch(gd);
            try {
                harness.getPermanentRemovalService().removePermanentToHand(gd, first);
                harness.getPermanentRemovalService().removePermanentToHand(gd, second);
            } finally {
                harness.getPermanentRemovalService().endPermanentLeaveBatch(gd);
            }
        });
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    @DisplayName("Does not get experience when a creature dies or an opponent's creature leaves")
    void ignoresDeathsAndOpponents() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dying));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, opponentCreature));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Creates one Ally token for each experience counter at upkeep")
    void createsAllyTokensForExperienceCounters() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        gd.playerExperienceCounters.put(player1.getId(), 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        List<Permanent> allies = findPermanents(player1, "Ally");
        assertThat(allies).hasSize(2);
        assertThat(allies).allMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ALLY));
    }

    @Test
    void cannotAirbendItself() {
        harness.setHand(player1, List.of(new AangAirbendingMaster()));
        addAangMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Aang, Airbending Master");
        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    @Test
    void airbendingOwnCreatureGrantsExperienceAndAllowsCastingForTwoGenericMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AangAirbendingMaster()));
        addAangMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent aang = findPermanent(player1, "Aang, Airbending Master");
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId()).doesNotContain(aang.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, target.getOriginalCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void gainsExperienceWhenAangItselfLeavesWithoutDying() {
        Permanent aang = harness.addToBattlefieldAndReturn(player1, new AangAirbendingMaster());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aang));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aang, Airbending Master");
        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void separateLeaveEventsGrantSeparateExperienceCounters() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, first));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, second));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
    }

    @Test
    void upkeepUsesExperienceCountAtResolution() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerExperienceCounters.put(player1.getId(), 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 2);
        assertThat(findPermanents(player1, "Ally")).hasSize(2);
    }

    @Test
    void createsNoTokensWithoutExperienceCounters() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).isEmpty();
    }

    @Test
    void doesNotCreateTokensOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        gd.playerExperienceCounters.put(player1.getId(), 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Ally")).isEmpty();
        assertThat(findPermanents(player2, "Ally")).isEmpty();
    }

    @Test
    void doesNotGainExperienceWhileDressDownRemovesItsAbilities() {
        harness.addToBattlefield(player1, new AangAirbendingMaster());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DressDown());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerExperienceCounters).doesNotContainKey(player1.getId());
    }

    private void addAangMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
