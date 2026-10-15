package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrucibleOfWorlds;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mobilization;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AgainstAllOdds.class, GrizzlyBears.class, HolyDay.class, LlanowarElves.class,
        CrucibleOfWorlds.class, HillGiant.class, Mobilization.class})
class AgainstAllOddsTest extends BaseCardTest {

    @Test
    @DisplayName("Flicker mode exiles and returns an artifact or creature you control")
    void flickersControlledArtifactOrCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, -1, List.of(bearsId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
    }

    @Test
    @DisplayName("Graveyard mode returns a qualifying artifact or creature")
    void returnsQualifyingCardFromGraveyard() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, -2, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Both modes resolve with their independent targets")
    void resolvesBothModes() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        var bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        gs.playCard(gd, player1, 0, -3, creature.getId(), null,
                List.of(bearsId), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
    }

    @Test
    @DisplayName("Targets must match the selected mode")
    void rejectsIllegalTargets() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, -2, instant.getId(), null, List.of(), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, -1, null, null,
                List.of(harness.getPermanentId(player2, "Grizzly Bears")), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flickersNoncreatureArtifact() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        prepareSpell();
        var artifactId = harness.getPermanentId(player1, "Crucible of Worlds");

        harness.castSorcery(player1, 0, -1, List.of(artifactId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crucible of Worlds");
        assertThat(harness.getPermanentId(player1, "Crucible of Worlds")).isNotEqualTo(artifactId);
    }

    @Test
    void returnsNoncreatureArtifactWithManaValueThree() {
        Card artifact = new CrucibleOfWorlds();
        harness.setGraveyard(player1, List.of(artifact));
        prepareSpell();

        harness.castAndResolveSorcery(player1, 0, -2, artifact.getId());

        harness.assertOnBattlefield(player1, "Crucible of Worlds");
        harness.assertNotInGraveyard(player1, "Crucible of Worlds");
    }

    @Test
    void rejectsCreatureWithManaValueFour() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, -2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsCardInOpponentsGraveyard() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player2, List.of(creature));
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, -2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsStolenCreatureUnderOwnersControl() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());
        prepareSpell();

        harness.castSorcery(player1, 0, -1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void stillFlickersWhenGraveyardTargetLeavesBeforeResolution() {
        Card creature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new GrizzlyBears());
        prepareSpell();
        var bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        gs.playCard(gd, player1, 0, -3, creature.getId(), null, List.of(bearsId), List.of());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
    }

    @Test
    void exiledTokenDoesNotReturn() {
        harness.addToBattlefield(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        var soldierId = harness.getPermanentId(player1, "Soldier");
        prepareSpell();

        harness.castSorcery(player1, 0, -1, List.of(soldierId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Soldier");
        harness.assertOnBattlefield(player1, "Mobilization");
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new AgainstAllOdds()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
