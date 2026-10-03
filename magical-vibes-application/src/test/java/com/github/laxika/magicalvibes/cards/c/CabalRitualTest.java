package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalRitual.class})
class CabalRitualTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving below threshold adds three black mana")
    void resolvingBelowThresholdAddsThreeBlackMana() {
        castCabalRitual();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving with six graveyard cards stays below threshold")
    void resolvingWithSixGraveyardCardsStaysBelowThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        castCabalRitual();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving at threshold adds five black mana")
    void resolvingAtThresholdAddsFiveBlackMana() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        castCabalRitual();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(5);
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player2, graveyardWithSevenCards());
        castCabalRitual();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
    }

    @Test
    @DisplayName("A ritual resolving first can enable threshold for another ritual on the stack")
    void earlierResolutionEnablesThreshold() {
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));
        castCabalRitual();
        castCabalRitual();

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(8);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(8);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing threshold before resolution adds only three black mana")
    void losingThresholdBeforeResolutionAddsThreeBlackMana() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        castCabalRitual();
        harness.setGraveyard(player1, graveyardWithSevenCards().subList(0, 6));

        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }

    private void castCabalRitual() {
        harness.castFromHand(player1, new CabalRitual(), "{1}{B}");
    }

    private List<Card> graveyardWithSevenCards() {
        return List.of(
                new CabalRitual(), new CabalRitual(), new CabalRitual(), new CabalRitual(),
                new CabalRitual(), new CabalRitual(), new CabalRitual());
    }
}
