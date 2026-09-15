package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaritLagesSlumber.class, SnowCoveredIsland.class, Forest.class})
class MaritLagesSlumberTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 1 when it or another snow permanent you control enters")
    void scriesForOwnSnowPermanentEntries() {
        Card top = new Forest();
        Card bottom = new SnowCoveredIsland();
        harness.setLibrary(player1, List.of(top, bottom));

        harness.enterBattlefieldAndReturn(player1, new MaritLagesSlumber());
        resolveScryToBottom();

        harness.enterBattlefieldAndReturn(player1, new SnowCoveredIsland());
        resolveScryToBottom();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
    }

    @Test
    @DisplayName("Does not trigger for nonsnow or opposing snow permanents")
    void ignoresNonsnowAndOpposingSnowEntries() {
        harness.addToBattlefield(player1, new MaritLagesSlumber());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player2, new SnowCoveredIsland());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices itself and creates Marit Lage at ten snow permanents")
    void sacrificesAndCreatesMaritLageAtSnowThreshold() {
        Permanent slumber = harness.addToBattlefieldAndReturn(player1, new MaritLagesSlumber());
        addSnowPermanents(9);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(slumber.getId()));
        harness.assertInGraveyard(player1, "Marit Lage's Slumber");

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Marit Lage"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getPower()).isEqualTo(20);
        assertThat(token.getCard().getToughness()).isEqualTo(20);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.AVATAR);
        assertThat(token.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Does not sacrifice itself below ten snow permanents")
    void doesNotSacrificeBelowSnowThreshold() {
        Permanent slumber = harness.addToBattlefieldAndReturn(player1, new MaritLagesSlumber());
        addSnowPermanents(8);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(slumber.getId()));
    }

    private void addSnowPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new SnowCoveredIsland());
        }
    }

    private void resolveScryToBottom() {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
    }
}
