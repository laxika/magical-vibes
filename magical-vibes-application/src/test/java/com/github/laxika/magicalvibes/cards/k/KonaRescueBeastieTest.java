package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KonaRescueBeastie.class, GrizzlyBears.class, Forest.class, LightningBolt.class})
class KonaRescueBeastieTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped Kona may put a permanent card from hand onto the battlefield")
    void tappedKonaPutsPermanentFromHandOntoBattlefield() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new LightningBolt();
        harness.setHand(player1, List.of(creature, land, instant));
        kona.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, instant);
    }

    @Test
    @DisplayName("Kona's controller may decline its Survival ability")
    void mayDeclinePuttingPermanent() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        kona.tap();

        advanceToPostcombatMain();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    @DisplayName("An untapped Kona does not trigger Survival")
    void untappedKonaDoesNotTrigger() {
        harness.addToBattlefield(player1, new KonaRescueBeastie());
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Untapping Kona before its Survival ability resolves prevents the effect")
    void untappingBeforeResolutionPreventsEffect() {
        Permanent kona = harness.addToBattlefieldAndReturn(player1, new KonaRescueBeastie());
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        kona.tap();

        advanceToPostcombatMain();
        kona.untap();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
