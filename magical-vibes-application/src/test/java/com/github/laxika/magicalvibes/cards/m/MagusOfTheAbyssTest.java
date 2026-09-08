package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagusOfTheAbyss.class, GrizzlyBears.class, IronMyr.class})
class MagusOfTheAbyssTest extends BaseCardTest {

    @Test
    @DisplayName("The active player chooses a nonartifact creature they control")
    void activePlayerChoosesCreatureToDestroy() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        Permanent controllerCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent validCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new IronMyr());

        advanceToUpkeep(player2);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(validCreature.getId())
                .doesNotContain(controllerCreature.getId(), artifactCreature.getId());

        harness.handlePermanentChosen(player2, validCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Iron Myr");
    }

    @Test
    @DisplayName("The destruction cannot be regenerated")
    void destructionCannotBeRegenerated() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setRegenerationShield(1);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger does not exist without a legal active-player creature")
    void doesNotTriggerWithoutLegalCreature() {
        harness.addToBattlefield(player1, new MagusOfTheAbyss());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IronMyr());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
