package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshnodsTransmogrant;
import com.github.laxika.magicalvibes.cards.c.CoalGolem;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.cards.s.SpectralCloak;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheAbyss.class, KoboldsOfKherKeep.class, CoalGolem.class,
        AshnodsTransmogrant.class, SpectralCloak.class})
class TheAbyssTest extends BaseCardTest {

    @Test
    void activePlayerChoosesAControlledNonartifactCreatureAndItCannotRegenerate() {
        harness.addToBattlefield(player1, new TheAbyss());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        Permanent activeCreature = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());
        Permanent activeArtifactCreature = harness.addToBattlefieldAndReturn(player2, new CoalGolem());
        activeCreature.setRegenerationShield(1);

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(activeCreature.getId());

        harness.handlePermanentChosen(player2, activeCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(controllerCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .containsExactly(activeArtifactCreature);
    }

    @Test
    void triggersDuringTheControllerUpkeepAsWell() {
        harness.addToBattlefield(player1, new TheAbyss());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        Permanent nonActiveCreature = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(controllerCreature.getId())
                .doesNotContain(nonActiveCreature.getId());

        harness.handlePermanentChosen(player1, controllerCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kobolds of Kher Keep");
        harness.assertOnBattlefield(player2, "Kobolds of Kher Keep");
    }

    @Test
    void doesNotTriggerWithoutALegalCreature() {
        harness.addToBattlefield(player1, new TheAbyss());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        Permanent activeArtifactCreature = harness.addToBattlefieldAndReturn(player2, new CoalGolem());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(controllerCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(activeArtifactCreature);
    }

    @Test
    void activePlayerChoosesOnlyOneOfSeveralLegalCreatures() {
        harness.addToBattlefield(player1, new TheAbyss());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void targetBecomingAnArtifactBeforeResolutionSurvives() {
        harness.addToBattlefield(player1, new TheAbyss());
        harness.addToBattlefield(player2, new AshnodsTransmogrant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, creature.getId());

        harness.activateAbility(player2, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(creature)).isTrue();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player2, "Ashnod's Transmogrant");
        harness.assertNotInGraveyard(player2, "Kobolds of Kher Keep");
    }

    @Test
    void shroudedCreatureCannotBeChosenEvenByItsController() {
        harness.addToBattlefield(player1, new TheAbyss());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());
        Permanent unprotectedCreature = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep());
        Permanent cloak = harness.addToBattlefieldAndReturn(player2, new SpectralCloak());
        cloak.setAttachedTo(protectedCreature.getId());

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(unprotectedCreature.getId());

        harness.handlePermanentChosen(player2, unprotectedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(protectedCreature, cloak);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
