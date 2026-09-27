package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpeciesSpecialist.class, GrizzlyBears.class, Forest.class})
class SpeciesSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("A creature of the chosen type dying lets the controller draw")
    void matchingCreatureDies() {
        Permanent specialist = addSpecialist(CardSubtype.BEAR);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        kill(bear);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(specialist.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("A creature of another type dying does not trigger a draw")
    void differentCreatureTypeDoesNotTrigger() {
        addSpecialist(CardSubtype.ELF);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        kill(bear);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("The Specialist can trigger for its own death")
    void ownDeathTriggersWhenChosenTypeMatches() {
        Permanent specialist = addSpecialist(CardSubtype.HUMAN);
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        kill(specialist);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    private Permanent addSpecialist(CardSubtype chosenSubtype) {
        Permanent specialist = harness.addToBattlefieldAndReturn(player1, new SpeciesSpecialist());
        specialist.setChosenSubtype(chosenSubtype);
        return specialist;
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        resolveAllTriggers();
    }
}
