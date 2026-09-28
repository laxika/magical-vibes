package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightsCharge.class, GrizzlyBears.class})
class KnightsChargeTest extends BaseCardTest {

    @Test
    @DisplayName("A Knight attacking makes each opponent lose 1 life and its controller gain 1 life")
    void knightAttackDrainsOpponentsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addKnightsCharge(player1);
        addKnightReady(player1);

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A non-Knight attacking does not trigger Knights' Charge")
    void nonKnightAttackDoesNotTrigger() {
        addKnightsCharge(player1);
        addNonKnightReady(player1);

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Knights' Charge returns all Knight creature cards from its controller's graveyard")
    void sacrificesAndReturnsAllKnights() {
        Card knight = new Card();
        knight.setName("Test Knight");
        knight.setType(CardType.CREATURE);
        knight.setManaCost("{W}");
        knight.setColor(CardColor.WHITE);
        knight.setSubtypes(List.of(CardSubtype.KNIGHT));
        knight.setPower(0);
        knight.setToughness(1);
        Card nonKnight = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(knight, nonKnight));
        Permanent charge = addKnightsCharge(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(charge);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(knight.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(nonKnight, charge.getCard())
                .doesNotContain(knight);
    }

    private Permanent addKnightsCharge(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KnightsCharge());
    }

    private Permanent addKnightReady(Player player) {
        Card knight = new Card();
        knight.setName("Test Knight");
        knight.setType(CardType.CREATURE);
        knight.setColor(CardColor.WHITE);
        knight.setSubtypes(List.of(CardSubtype.KNIGHT));
        knight.setPower(0);
        knight.setToughness(1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player, knight);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addNonKnightReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
