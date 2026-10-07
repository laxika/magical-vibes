package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UndergroundMortuary.class})
class UndergroundMortuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new UndergroundMortuary();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new UndergroundMortuary()));

        harness.playLand(player1, 0);
        Permanent mortuary = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mortuary.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Surveil can leave the top card in the library")
    void canKeepTopCard() {
        Card topCard = new UndergroundMortuary();
        Card nextCard = new UndergroundMortuary();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new UndergroundMortuary()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil with an empty library completes without a choice")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new UndergroundMortuary()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil still resolves after the land leaves the battlefield")
    void surveilResolvesWithoutSource() {
        Card topCard = new UndergroundMortuary();
        Card nextCard = new UndergroundMortuary();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new UndergroundMortuary()));

        harness.playLand(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Taps for black mana")
    void tapsForBlackMana() {
        tapFor(ManaColor.BLACK);
    }

    @Test
    @DisplayName("Taps for green mana")
    void tapsForGreenMana() {
        tapFor(ManaColor.GREEN);
    }

    private void tapFor(ManaColor color) {
        Permanent mortuary = addReadyMortuary();

        harness.activateAbility(player1, 0, color == ManaColor.BLACK ? 0 : 1, null, null);

        assertThat(mortuary.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    private Permanent addReadyMortuary() {
        Permanent mortuary = harness.addToBattlefieldAndReturn(player1, new UndergroundMortuary());
        mortuary.setSummoningSick(false);
        return mortuary;
    }
}
