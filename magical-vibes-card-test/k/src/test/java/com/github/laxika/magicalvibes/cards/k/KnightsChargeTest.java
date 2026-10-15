package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.r.RagingRedcap;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightsCharge.class, YouthfulKnight.class, RagingRedcap.class, Gingerbrute.class})
class KnightsChargeTest extends BaseCardTest {

    @Test
    @DisplayName("A Knight attacking makes each opponent lose 1 life and its controller gain 1 life")
    void knightAttackDrainsOpponentsAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addKnightsCharge(player1);
        addCreatureReady(player1, new YouthfulKnight());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A non-Knight attacking does not trigger Knights' Charge")
    void nonKnightAttackDoesNotTrigger() {
        addKnightsCharge(player1);
        addCreatureReady(player1, new Gingerbrute());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Knights' Charge returns all Knight creature cards from its controller's graveyard")
    void sacrificesAndReturnsAllKnights() {
        Card knight = new YouthfulKnight();
        Card nonKnight = new Gingerbrute();
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

    @Test
    @DisplayName("Each attacking Knight creates one complete drain trigger")
    void multipleKnightsEachTriggerOnce() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addKnightsCharge(player1);
        addCreatureReady(player1, new YouthfulKnight());
        addCreatureReady(player1, new RagingRedcap());
        addCreatureReady(player1, new Gingerbrute());

        declareAttackers(List.of(1, 2, 3));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("An opponent's attacking Knight does not trigger Knights' Charge")
    void opposingKnightDoesNotTrigger() {
        addKnightsCharge(player1);
        addCreatureReady(player2, new YouthfulKnight());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All Knights return automatically, untapped, from only the controller's graveyard")
    void returnsEveryKnightWithoutOfferingAChoice() {
        Card firstKnight = new YouthfulKnight();
        Card secondKnight = new RagingRedcap();
        Card nonKnight = new Gingerbrute();
        Card opposingKnight = new YouthfulKnight();
        harness.setGraveyard(player1, List.of(firstKnight, secondKnight, nonKnight));
        harness.setGraveyard(player2, List.of(opposingKnight));
        Permanent charge = addKnightsCharge(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(charge.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstKnight.getId(), secondKnight.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.isTapped() && permanent.isSummoningSick());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonKnight, charge.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingKnight);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability can be activated with no Knight cards in the graveyard")
    void activationWithNoKnightsStillPaysSacrificeCost() {
        Card nonKnight = new Gingerbrute();
        harness.setGraveyard(player1, List.of(nonKnight));
        Permanent charge = addKnightsCharge(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonKnight, charge.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addKnightsCharge(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KnightsCharge());
    }

}
