package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Etherium Abomination")
class EtheriumAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("Unearth returns Etherium Abomination to the battlefield with haste")
    void unearthReturnsWithHaste() {
        EtheriumAbomination card = new EtheriumAbomination();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Etherium Abomination");
        assertThat(perm.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Etherium Abomination");
    }

    @Test
    @DisplayName("Unearthed Etherium Abomination is exiled at the next end step")
    void unearthExiledAtEndStep() {
        EtheriumAbomination card = new EtheriumAbomination();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Etherium Abomination");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Etherium Abomination"));
    }
}
