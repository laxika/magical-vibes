package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Madlands.class, RavensCrime.class})
class MadlandsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when played from hand")
    void entersTappedFromHand() {
        harness.setHand(player1, List.of(new Madlands()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Madlands").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Produces black and red mana")
    void producesBlackAndRedMana() {
        harness.addToBattlefield(player1, new Madlands());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Can be played from exile for its zero-cost madness ability")
    void playsFromExileForMadness() {
        Madlands madlands = new Madlands();
        harness.setHand(player1, List.of(new RavensCrime(), madlands));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent permanent = findPermanent(player1, "Madlands");
        assertThat(permanent.getCard().getId()).isEqualTo(madlands.getId());
        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(madlands.getId()));
    }
}
