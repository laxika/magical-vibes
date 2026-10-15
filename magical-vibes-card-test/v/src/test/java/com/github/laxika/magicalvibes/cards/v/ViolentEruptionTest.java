package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.InvasionOfRegatha;
import com.github.laxika.magicalvibes.cards.p.PardicCollaborator;
import com.github.laxika.magicalvibes.cards.u.Unhinge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ViolentEruption.class, PardicCollaborator.class, Unhinge.class, InvasionOfRegatha.class})
class ViolentEruptionTest extends BaseCardTest {

    @Test
    void dividesFourDamageAmongCreatureAndPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PardicCollaborator());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new ViolentEruption()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(creature.getId(), 2, player2.getId(), 2));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pardic Collaborator");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    void madnessDealsFourDamageAfterBeingDiscarded() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PardicCollaborator());
        discardViaUnhinge();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Pardic Collaborator");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Violent Eruption");
    }

    @Test
    void decliningMadnessPutsItIntoGraveyard() {
        ViolentEruption eruption = discardViaUnhinge();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(eruption.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(eruption.getId()));
        harness.assertInGraveyard(player1, "Violent Eruption");
    }

    @Test
    void dividesDamageAmongFourTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PardicCollaborator());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new PardicCollaborator());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ViolentEruption()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(first.getId(), 1, second.getId(), 1,
                player1.getId(), 1, player2.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Pardic Collaborator");
        harness.assertOnBattlefield(player2, "Pardic Collaborator");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Violent Eruption");
    }

    @Test
    void rejectsZeroDamageAssignedToATarget() {
        harness.setHand(player1, List.of(new ViolentEruption()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                Map.of(player1.getId(), 0, player2.getId(), 4)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Violent Eruption");
    }

    @Test
    void cannotCastForMadnessWithoutEnoughMana() {
        discardViaUnhinge();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Violent Eruption");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void madnessAllowsChoosingMultipleDamageTargets() {
        harness.addToBattlefield(player2, new PardicCollaborator());
        discardViaUnhinge();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
    }

    @Test
    void madnessOffersBattleAsLegalDamageTarget() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        discardViaUnhinge();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction interaction = gd.interaction.activeInteraction();
        if (interaction instanceof PendingInteraction.PermanentChoice choice) {
            assertThat(choice.validIds()).contains(battle.getId());
        } else {
            assertThat(interaction).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
            PendingInteraction.MultiPermanentChoice choice = (PendingInteraction.MultiPermanentChoice) interaction;
            assertThat(choice.validIds()).contains(battle.getId());
        }
    }

    private ViolentEruption discardViaUnhinge() {
        ViolentEruption eruption = new ViolentEruption();
        harness.setHand(player1, List.of(eruption));
        harness.setHand(player2, List.of(new Unhinge()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return eruption;
    }
}
