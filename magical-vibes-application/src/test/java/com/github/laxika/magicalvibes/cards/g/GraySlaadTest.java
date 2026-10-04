package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EntropicDecay;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraySlaad.class, EntropicDecay.class, Swamp.class})
class GraySlaadTest extends BaseCardTest {

    @Test
    void adventureMillsFourAndExilesTheCard() {
        GraySlaad card = new GraySlaad();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(
                new GraySlaad(), new GraySlaad(), new GraySlaad(), new GraySlaad()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void gainsMenaceAndDeathtouchWithFourCreatureCardsInGraveyard() {
        Permanent slaad = harness.addToBattlefieldAndReturn(player1, new GraySlaad());

        assertThat(gqs.hasKeyword(gd, slaad, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, slaad, Keyword.DEATHTOUCH)).isFalse();

        harness.setGraveyard(player1, List.of(
                new GraySlaad(), new GraySlaad(), new GraySlaad(), new GraySlaad()));

        assertThat(gqs.hasKeyword(gd, slaad, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slaad, Keyword.DEATHTOUCH)).isTrue();

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.hasKeyword(gd, slaad, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, slaad, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        GraySlaad card = new GraySlaad();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(
                new GraySlaad(), new GraySlaad(), new GraySlaad(), new GraySlaad()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void opponentsCreatureCardsAndOwnNoncreatureCardsDoNotMeetThreshold() {
        Permanent slaad = harness.addToBattlefieldAndReturn(player1, new GraySlaad());
        harness.setGraveyard(player1, List.of(
                new GraySlaad(), new GraySlaad(), new GraySlaad(), new Swamp()));
        harness.setGraveyard(player2, List.of(
                new GraySlaad(), new GraySlaad(), new GraySlaad(), new GraySlaad()));

        assertThat(gqs.hasKeyword(gd, slaad, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, slaad, Keyword.DEATHTOUCH)).isFalse();

        harness.setGraveyard(player1, List.of(
                new GraySlaad(), new GraySlaad(), new GraySlaad(), new GraySlaad(), new GraySlaad()));

        assertThat(gqs.hasKeyword(gd, slaad, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slaad, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void adventureMillsOnlyAvailableCardsAndLeavesOpponentsLibraryAlone() {
        GraySlaad card = new GraySlaad();
        GraySlaad creature = new GraySlaad();
        Swamp land = new Swamp();
        GraySlaad opponentsCard = new GraySlaad();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(creature, land));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }
}
