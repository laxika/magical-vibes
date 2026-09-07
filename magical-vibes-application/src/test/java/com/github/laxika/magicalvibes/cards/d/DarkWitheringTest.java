package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarkWithering.class, GrizzlyBears.class, RavensCrime.class, WalkingCorpse.class})
class DarkWitheringTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonblack creature")
    void destroysTargetNonblackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDarkWithering(target, 2, 4);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new DarkWithering()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Discarding Dark Withering offers its madness cost")
    void discardTriggersMadness() {
        DarkWithering darkWithering = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(darkWithering.getId()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Casting Dark Withering for madness destroys a target nonblack creature")
    void castingForMadnessDestroysTargetNonblackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dark Withering");
    }

    private void castDarkWithering(Permanent target, int blackMana, int genericMana) {
        harness.setHand(player1, List.of(new DarkWithering()));
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private DarkWithering discardViaRavensCrime() {
        DarkWithering darkWithering = new DarkWithering();
        harness.setHand(player1, List.of(darkWithering));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return darkWithering;
    }
}
