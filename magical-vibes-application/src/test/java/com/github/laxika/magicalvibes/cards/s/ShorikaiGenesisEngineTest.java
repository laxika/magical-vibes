package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.Weatherlight;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShorikaiGenesisEngine.class, Forest.class, GrizzlyBears.class, Weatherlight.class})
class ShorikaiGenesisEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, discards one, and creates an enhanced Pilot")
    void drawsDiscardsAndCreatesPilot() {
        addShorikai();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPilot()).isNotNull();
    }

    @Test
    @DisplayName("The Pilot contributes two additional power when crewing")
    void pilotEnhancesCrewPower() {
        addShorikai();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent pilot = findPilot();
        pilot.setSummoningSick(false);
        Permanent weatherlight = harness.addToBattlefieldAndReturn(player1, new Weatherlight());
        weatherlight.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weatherlight), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, weatherlight)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew 8 animates Shorikai with eight total creature power")
    void crewEightAnimatesShorikai() {
        addShorikai();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent shorikai = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, shorikai)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).subList(1, 5))
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    private void addShorikai() {
        Permanent shorikai = harness.addToBattlefieldAndReturn(player1, new ShorikaiGenesisEngine());
        shorikai.setSummoningSick(false);
    }

    private Permanent findPilot() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PILOT))
                .findFirst()
                .orElseThrow();
    }
}
