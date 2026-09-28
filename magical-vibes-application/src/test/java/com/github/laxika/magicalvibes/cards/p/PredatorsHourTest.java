package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
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

@CardUsed({PredatorsHour.class, Divination.class, Forest.class, GrizzlyBears.class})
class PredatorsHourTest extends BaseCardTest {

    @Test
    @DisplayName("Own creatures gain menace and exile the damaged player's top card")
    void grantsMenaceAndExilesTopCard() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card topCard = new Divination();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new PredatorsHour()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(attacker.hasKeyword(Keyword.MENACE)).isTrue();

        attacker.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        Card exiledCard = gd.getCardsExiledByPermanent(attacker.getId()).getFirst();
        ExiledCardEntry exiled = gd.findExiledCard(exiledCard.getId());
        assertThat(exiledCard).isSameAs(topCard);
        assertThat(exiled.faceDown()).isTrue();

        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The temporary grants wear off at end of turn")
    void grantsWearOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PredatorsHour()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(opponentCreature.hasKeyword(Keyword.MENACE)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.hasKeyword(Keyword.MENACE)).isFalse();
    }
}
