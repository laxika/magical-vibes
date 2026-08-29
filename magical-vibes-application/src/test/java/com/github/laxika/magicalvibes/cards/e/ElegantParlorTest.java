package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({ElegantParlor.class, GrizzlyBears.class})
class ElegantParlorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ElegantParlor()));

        harness.playLand(player1, 0);
        Permanent parlor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(parlor.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        tapFor(ManaColor.RED);
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    private void tapFor(ManaColor color) {
        Permanent parlor = addReadyParlor();

        harness.activateAbility(player1, 0, color == ManaColor.RED ? 0 : 1, null, null);

        assertThat(parlor.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

    private Permanent addReadyParlor() {
        Permanent parlor = new Permanent(new ElegantParlor());
        parlor.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(parlor);
        return parlor;
    }
}
