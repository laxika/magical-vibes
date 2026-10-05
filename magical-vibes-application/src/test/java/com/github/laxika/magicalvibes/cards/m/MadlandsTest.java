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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.castAndResolveSorcery(player1, 0, player1.getId());
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

    @Test
    @DisplayName("Produces red mana immediately and taps as its cost")
    void producesRedMana() {
        harness.addToBattlefield(player1, new Madlands());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(findPermanent(player1, "Madlands").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining madness puts the discarded land into its owner's graveyard")
    void decliningMadnessPutsLandInGraveyard() {
        Madlands madlands = new Madlands();
        harness.setHand(player1, List.of(new RavensCrime(), madlands));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(madlands);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Madlands");
        harness.assertNotOnBattlefield(player1, "Madlands");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(madlands);
    }

    @Test
    @DisplayName("Madness cannot play a land after the turn's land play is spent")
    void madnessRespectsLandPlayLimit() {
        Madlands discarded = new Madlands();
        harness.setHand(player1, List.of(new Madlands(), new RavensCrime(), discarded));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Madlands")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Madlands");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
    }

    @Test
    @DisplayName("Madness cannot play a land during an opponent's turn")
    void madnessRespectsActivePlayer() {
        Madlands madlands = new Madlands();
        harness.setHand(player1, List.of(new RavensCrime()));
        harness.setHand(player2, List.of(madlands));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Madlands");
        harness.assertInGraveyard(player2, "Madlands");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(madlands);
    }

    @Test
    @DisplayName("Playing a land through madness consumes the normal land play")
    void madnessConsumesLandPlay() {
        harness.setHand(player1, List.of(new RavensCrime(), new Madlands()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.setHand(player1, List.of(new Madlands()));

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Madlands")).isEqualTo(1);
        harness.assertInHand(player1, "Madlands");
    }
}
