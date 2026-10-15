package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoneRetrievalUnit.class})
class StoneRetrievalUnitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a tapped Powerstone token")
    void etbCreatesTappedPowerstone() {
        harness.setHand(player1, List.of(new StoneRetrievalUnit()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }
    @Test
    void tokenIsCreatedOnlyWhenEnterTriggerResolves() {
        harness.setHand(player1, List.of(new StoneRetrievalUnit()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Stone Retrieval Unit");
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard() instanceof StoneRetrievalUnit);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    void powerstoneManaCanPayForAnotherStoneRetrievalUnit() {
        harness.setHand(player1, List.of(new StoneRetrievalUnit()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.performUntapStep(player1);
        Permanent powerstone = findPermanents(player1, "Powerstone").getFirst();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(powerstone);
        harness.activateAbility(player1, tokenIndex, null, null);
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();

        harness.setHand(player1, List.of(new StoneRetrievalUnit()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Stone Retrieval Unit")).hasSize(2);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }
}
