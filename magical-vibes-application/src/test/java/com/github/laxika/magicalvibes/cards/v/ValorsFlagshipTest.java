package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValorsFlagshipTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling for X creates X enhanced Pilot tokens and draws a card")
    void cyclingCreatesPilotsAndDraws() {
        harness.setHand(player1, List.of(new ValorsFlagship()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null, 2);
        harness.passBothPriorities();

        List<Permanent> pilots = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PILOT))
                .toList();
        assertThat(pilots).hasSize(2);
        assertThat(pilots).allSatisfy(pilot -> {
            assertThat(pilot.getCard().getPower()).isEqualTo(1);
            assertThat(pilot.getCard().getToughness()).isEqualTo(1);
        });
        harness.assertInGraveyard(player1, "Valor's Flagship");
        harness.assertInHand(player1, "Grizzly Bears");

        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        pilots.get(0).setSummoningSick(false);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.handlePermanentChosen(player1, pilots.get(0).getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilots.get(0).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew 3 animates Valor's Flagship")
    void crewAnimatesFlagship() {
        Permanent flagship = harness.addToBattlefieldAndReturn(player1, new ValorsFlagship());
        flagship.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        bears.setSummoningSick(false);
        memnite.setSummoningSick(false);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(flagship), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, flagship)).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(memnite.isTapped()).isTrue();
    }
}
