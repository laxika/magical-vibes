package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.MikokoroCenterOfTheSea;
import com.github.laxika.magicalvibes.cards.m.MirenTheMoaningWell;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThoughtsOfRuin.class, MikokoroCenterOfTheSea.class, MirenTheMoaningWell.class,
        OboroPalaceInTheClouds.class, TombOfUrami.class})
class ThoughtsOfRuinTest extends BaseCardTest {

    private List<UUID> landIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    private void addSokLands(Player player) {
        harness.addToBattlefield(player, new MikokoroCenterOfTheSea());
        harness.addToBattlefield(player, new MirenTheMoaningWell());
        harness.addToBattlefield(player, new OboroPalaceInTheClouds());
        harness.addToBattlefield(player, new TombOfUrami());
    }

    @Test
    @DisplayName("Each player sacrifices as many lands as the caster has cards in hand")
    void eachPlayerSacrificesBasedOnCastersHand() {
        addSokLands(player1);
        addSokLands(player2);
        harness.setHand(player1, List.of(new ThoughtsOfRuin(), new ThoughtsOfRuin(), new ThoughtsOfRuin(),
                new ThoughtsOfRuin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        assertThat(player1Choice.maxCount()).isEqualTo(3);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 3));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(3);
        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 3));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("The opponent's hand size does not change the number of lands sacrificed")
    void usesCastersHandInsteadOfEachPlayersHand() {
        addSokLands(player1);
        addSokLands(player2);
        harness.setHand(player1, List.of(new ThoughtsOfRuin(), new ThoughtsOfRuin()));
        harness.setHand(player2, List.of(new ThoughtsOfRuin(), new ThoughtsOfRuin(), new ThoughtsOfRuin(),
                new ThoughtsOfRuin(), new ThoughtsOfRuin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));
        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        assertThat(player2Choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(3);
    }

    @Test
    @DisplayName("A caster with no cards in hand causes no land sacrifices")
    void noCardsInCastersHandMeansNoSacrifices() {
        addSokLands(player1);
        addSokLands(player2);
        harness.setHand(player1, List.of(new ThoughtsOfRuin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(4);
        assertThat(landCount(player2)).isEqualTo(4);
    }

    @Test
    @DisplayName("A player with too few lands sacrifices all of them after the other player chooses")
    void tooFewLandsAreSacrificedTogetherWithChosenLands() {
        harness.addToBattlefield(player1, new MikokoroCenterOfTheSea());
        addSokLands(player2);
        harness.setHand(player1, List.of(new ThoughtsOfRuin(), new ThoughtsOfRuin(), new ThoughtsOfRuin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Mikokoro, Center of the Sea");
        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isZero();
        assertThat(landCount(player2)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Mikokoro, Center of the Sea");
        harness.assertInGraveyard(player2, "Mikokoro, Center of the Sea");
        harness.assertInGraveyard(player2, "Miren, the Moaning Well");
    }

    @Test
    @DisplayName("Chosen lands remain on the battlefield until both players have chosen")
    void sacrificesWaitForBothPlayersChoices() {
        addSokLands(player1);
        addSokLands(player2);
        harness.setHand(player1, List.of(new ThoughtsOfRuin(), new ThoughtsOfRuin()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(landCount(player1)).isEqualTo(4);
        assertThat(landCount(player2)).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Mikokoro, Center of the Sea");
        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(3);
        assertThat(landCount(player2)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Mikokoro, Center of the Sea");
        harness.assertInGraveyard(player2, "Mikokoro, Center of the Sea");
    }

    @Test
    @DisplayName("Returning Oboro in response increases the hand size used on resolution")
    void countsCardsInHandAtResolution() {
        addSokLands(player1);
        addSokLands(player2);
        harness.setHand(player1, List.of(new ThoughtsOfRuin()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0);
        harness.activateAbility(player1, 2, 1, null, null);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Oboro, Palace in the Clouds");
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));
        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(2);
        assertThat(landCount(player2)).isEqualTo(3);
    }
}
