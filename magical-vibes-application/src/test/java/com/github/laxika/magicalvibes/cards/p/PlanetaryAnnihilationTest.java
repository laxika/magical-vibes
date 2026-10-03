package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlanetaryAnnihilation.class, Forest.class, GrizzlyBears.class})
class PlanetaryAnnihilationTest extends BaseCardTest {

    @Test
    void eachPlayerKeepsSixLandsSacrificesTheRestAndAllCreaturesTakeDamage() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
            harness.addToBattlefield(player2, new Forest());
        }
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPlanetaryAnnihilation();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.context())
                .isInstanceOf(MultiPermanentChoiceContext.EachPlayerChoosesLandsThenSacrificeRestChoice.class);
        var player1Lands = List.copyOf(firstChoice.validIds());
        var player1SacrificedLand = player1Lands.getLast();

        harness.handleMultiplePermanentsChosen(player1, player1Lands.subList(0, 6));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(player1SacrificedLand));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, secondChoice.validIds().subList(0, 6));

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(6);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(6);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void playersWithFewerThanSixLandsKeepAllOfThem() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPlanetaryAnnihilation();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castPlanetaryAnnihilation() {
        harness.setHand(player1, List.of(new PlanetaryAnnihilation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
