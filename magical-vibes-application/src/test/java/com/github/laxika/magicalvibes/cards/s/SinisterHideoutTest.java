package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SinisterHideout.class, GrizzlyBears.class})
class SinisterHideoutTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SinisterHideout()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Sinister Hideout").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping adds blue or black mana")
    void tappingAddsChosenMana() {
        Permanent blueHideout = addReadyHideout();
        Permanent blackHideout = addReadyHideout();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(blueHideout.isTapped()).isTrue();
        assertThat(blackHideout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying four mana and tapping surveils one")
    void paidAbilitySurveilsOne() {
        Permanent hideout = addReadyHideout();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(hideout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining surveil one leaves the top card on the library")
    void declinedSurveilLeavesTopCard() {
        Permanent hideout = addReadyHideout();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        int graveyardBefore = gd.playerGraveyards.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardBefore);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(hideout.isTapped()).isTrue();
    }

    private Permanent addReadyHideout() {
        Permanent hideout = new Permanent(new SinisterHideout());
        hideout.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hideout);
        return hideout;
    }
}
