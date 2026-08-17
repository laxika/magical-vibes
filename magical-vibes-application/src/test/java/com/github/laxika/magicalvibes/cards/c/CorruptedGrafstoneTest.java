package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.cards.i.IslandFishJasconius;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorruptedGrafstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new CorruptedGrafstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent grafstone = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(grafstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Automatically adds the only color represented in the controller's graveyard")
    void autoAddsSingleGraveyardColor() {
        harness.addToBattlefield(player1, new CorruptedGrafstone());
        Permanent grafstone = gd.playerBattlefields.get(player1.getId()).getFirst();
        grafstone.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new ForestBear()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Offers only colors represented by cards in the controller's graveyard")
    void offersColorsFromGraveyard() {
        harness.addToBattlefield(player1, new CorruptedGrafstone());
        Permanent grafstone = gd.playerBattlefields.get(player1.getId()).getFirst();
        grafstone.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new ForestBear(), new IslandFishJasconius()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("BLUE", "GREEN");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Produces no mana when the graveyard has no colored cards")
    void producesNoManaWithoutColoredGraveyardCard() {
        harness.addToBattlefield(player1, new CorruptedGrafstone());
        Permanent grafstone = gd.playerBattlefields.get(player1.getId()).getFirst();
        grafstone.setSummoningSick(false);
        harness.setGraveyard(player1, List.of(new CorruptedGrafstone()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
