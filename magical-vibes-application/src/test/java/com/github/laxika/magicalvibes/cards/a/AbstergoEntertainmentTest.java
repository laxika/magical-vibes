package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CodexShredder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AbstergoEntertainment.class, CodexShredder.class, GrizzlyBears.class, Shock.class})
class AbstergoEntertainmentTest extends BaseCardTest {

    @Test
    void producesColorlessMana() {
        harness.addToBattlefield(player1, new AbstergoEntertainment());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void producesManaOfAnyColorForGenericCost() {
        harness.addToBattlefield(player1, new AbstergoEntertainment());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void returnsHistoricCardThenExilesAllGraveyards() {
        Permanent abstergo = harness.addToBattlefieldAndReturn(player1, new AbstergoEntertainment());
        CodexShredder historic = new CodexShredder();
        Shock ownNonHistoric = new Shock();
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(historic, ownNonHistoric));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, historic.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(historic);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownNonHistoric);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(abstergo.getCard());
    }

    @Test
    void cannotTargetNonHistoricCard() {
        harness.addToBattlefield(player1, new AbstergoEntertainment());
        Shock nonHistoric = new Shock();
        harness.setGraveyard(player1, List.of(nonHistoric));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, nonHistoric.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
