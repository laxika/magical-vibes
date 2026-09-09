package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefiantSurvivor.class, GrizzlyBears.class, Forest.class})
class DefiantSurvivorTest extends BaseCardTest {

    @Test
    void tappedSurvivorManifestsDreadAtPostcombatMain() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        survivor.tap();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));

        advanceToPostcombatMain();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(graveyardCard);
    }

    @Test
    void untappedSurvivorDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPostcombatMain();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void untappingBeforeResolutionPreventsManifestingDread() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new DefiantSurvivor());
        survivor.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));

        advanceToPostcombatMain();
        survivor.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    private void advanceToPostcombatMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
