package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MiragePhalanx.class, GrizzlyBears.class})
class MiragePhalanxTest extends BaseCardTest {

    @Test
    @DisplayName("Each paired creature creates a hasty copy at the beginning of combat")
    void createsHastyCopyForEachPairedCreature() {
        Permanent bears = castAndPairWithBears();

        advanceToBeginningOfCombat();
        resolveAllTriggers();

        List<Permanent> mirageCopies = findPermanents(player1, "Mirage Phalanx").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        List<Permanent> bearCopies = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();

        assertThat(mirageCopies).hasSize(1);
        assertThat(bearCopies).hasSize(1);
        assertThat(mirageCopies.getFirst().getCard().getKeywords()).contains(Keyword.HASTE)
                .doesNotContain(Keyword.SOULBOND);
        assertThat(bearCopies.getFirst().getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(bears.getPairedWithId()).isNotNull();
    }

    @Test
    @DisplayName("The temporary copies are exiled at end of combat")
    void copiesAreExiledAtEndOfCombat() {
        castAndPairWithBears();
        advanceToBeginningOfCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mirage Phalanx"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mirage Phalanx"))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).isEmpty();
    }

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MiragePhalanx()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
