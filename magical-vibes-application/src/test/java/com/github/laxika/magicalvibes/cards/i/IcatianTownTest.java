package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(IcatianTown.class)
class IcatianTownTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castAndResolveIcatianTown() {
        prepareMain(player1);
        harness.castFromHand(player1, new IcatianTown(), "{5}{W}");
        harness.passBothPriorities();
    }

    private List<Permanent> citizenTokens() {
        return findPermanents(player1, "Citizen");
    }

    @Test
    @DisplayName("Resolving Icatian Town creates four Citizen tokens")
    void resolvingCreatesFourTokens() {
        castAndResolveIcatianTown();

        assertThat(citizenTokens()).hasSize(4);
        for (Permanent token : citizenTokens()) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        }
    }

    @Test
    @DisplayName("Icatian Town creates white Citizen creature tokens")
    void createsWhiteCitizenCreatureTokens() {
        castAndResolveIcatianTown();

        assertThat(citizenTokens()).hasSize(4).allSatisfy(token -> {
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        });
    }

    @Test
    @DisplayName("Icatian Town goes to the graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        castAndResolveIcatianTown();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Icatian Town");
    }
}
