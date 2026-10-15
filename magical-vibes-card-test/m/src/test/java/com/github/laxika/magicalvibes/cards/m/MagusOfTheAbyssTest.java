package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NewBenalia;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.q.Quagnoth;
import com.github.laxika.magicalvibes.cards.s.SarcomiteMyr;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheAbyss.class, NessianCourser.class, SarcomiteMyr.class, NewBenalia.class, Quagnoth.class})
class MagusOfTheAbyssTest extends BaseCardTest {

    @Test
    @DisplayName("The active player chooses a nonartifact creature they control")
    void activePlayerChoosesCreatureToDestroy() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent validCreature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new SarcomiteMyr());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new NewBenalia());

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(validCreature.getId())
                .doesNotContain(controllerCreature.getId(), artifactCreature.getId(), nonCreature.getId());

        harness.handlePermanentChosen(player2, validCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nessian Courser");
        harness.assertOnBattlefield(player1, "Nessian Courser");
        harness.assertOnBattlefield(player2, "Sarcomite Myr");
        harness.assertOnBattlefield(player2, "New Benalia");
    }

    @Test
    @DisplayName("The destruction cannot be regenerated")
    void destructionCannotBeRegenerated() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        creature.setRegenerationShield(1);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nessian Courser");
    }

    @Test
    @DisplayName("The trigger also fires during the controller's upkeep")
    void triggersDuringControllerUpkeep() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheAbyss());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        Permanent nonActiveCreature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(magus.getId(), controllerCreature.getId())
                .doesNotContain(nonActiveCreature.getId());

        harness.handlePermanentChosen(player1, controllerCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nessian Courser");
        harness.assertOnBattlefield(player2, "Nessian Courser");
    }

    @Test
    @DisplayName("The trigger does not exist without a legal active-player creature")
    void doesNotTriggerWithoutLegalCreature() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        harness.addToBattlefield(player1, new NessianCourser());
        harness.addToBattlefield(player2, new SarcomiteMyr());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nessian Courser");
        harness.assertOnBattlefield(player2, "Sarcomite Myr");
    }

    @Test
    @DisplayName("Magus can destroy itself during its controller's upkeep")
    void destroysItselfWhenItIsTheOnlyLegalCreature() {
        Permanent magus = harness.addToBattlefieldAndReturn(player1, new MagusOfTheAbyss());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, magus.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magus of the Abyss");
        harness.assertNotOnBattlefield(player1, "Magus of the Abyss");
    }

    @Test
    @DisplayName("The active player cannot choose a creature with shroud")
    void excludesShroudedCreatureFromTargetChoices() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        harness.addToBattlefield(player2, new Quagnoth());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Nessian Courser");
        harness.assertOnBattlefield(player2, "Quagnoth");
    }

    @Test
    @DisplayName("A target that gains shroud before resolution survives")
    void targetGainingShroudIsNotDestroyed() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, creature.getId());
        creature.getGrantedKeywords().add(Keyword.SHROUD);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nessian Courser");
        harness.assertNotInGraveyard(player2, "Nessian Courser");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An indestructible creature is a legal target but survives")
    void indestructibleCreatureCanBeChosenAndSurvives() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nessian Courser");
        harness.assertNotInGraveyard(player2, "Nessian Courser");
        assertThat(gd.stack).isEmpty();
    }
}
