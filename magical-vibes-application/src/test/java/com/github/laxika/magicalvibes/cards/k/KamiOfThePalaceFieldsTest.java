package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RendSpirit;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfThePalaceFields.class, RendSpirit.class, KamiOfThePaintedRoad.class,
        SakuraTribeElder.class})
class KamiOfThePalaceFieldsTest extends BaseCardTest {

    private void killKami() {
        harness.setHand(player1, List.of(new RendSpirit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Kami of the Palace Fields"));
    }

    @Test
    @DisplayName("Soulshift targets a Spirit card with mana value 5 or less from its controller's graveyard")
    void soulshiftReturnsEligibleSpirit() {
        harness.addToBattlefield(player1, new KamiOfThePalaceFields());
        Card eligible = new KamiOfThePaintedRoad();
        Card nonSpirit = new SakuraTribeElder();
        Card tooExpensive = new KamiOfThePalaceFields();
        Card opponentSpirit = new KamiOfThePaintedRoad();
        harness.setGraveyard(player1, List.of(eligible, nonSpirit, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kami of the Painted Road");
        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
        harness.assertInGraveyard(player1, "Kami of the Palace Fields");
        harness.assertInGraveyard(player2, "Kami of the Painted Road");
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new KamiOfThePalaceFields());
        Card eligible = new KamiOfThePaintedRoad();
        harness.setGraveyard(player1, List.of(eligible));

        killKami();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kami of the Painted Road");
        harness.assertNotInHand(player1, "Kami of the Painted Road");
    }

    @Test
    @DisplayName("Soulshift does not trigger a graveyard choice without a legal target")
    void noEligibleTargetMeansNoChoice() {
        harness.addToBattlefield(player1, new KamiOfThePalaceFields());
        harness.setGraveyard(player1, List.of(new SakuraTribeElder(), new KamiOfThePalaceFields()));

        killKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
