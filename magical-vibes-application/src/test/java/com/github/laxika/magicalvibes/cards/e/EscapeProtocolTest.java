package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GusthasScepter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EscapeProtocol.class, Censor.class, GrizzlyBears.class, GusthasScepter.class})
class EscapeProtocolTest extends BaseCardTest {

    @Test
    void paysThenChoosesAnArtifactOrCreatureYouControl() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, creature.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
    }

    @Test
    void canFlickerAnArtifact() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GusthasScepter());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(harness.getPermanentId(player1, "Gustha's Scepter")).isNotEqualTo(artifact.getId());
    }

    @Test
    void decliningPaymentDoesNotFlicker() {
        harness.addToBattlefield(player1, new EscapeProtocol());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(creature.getId());
    }
}
