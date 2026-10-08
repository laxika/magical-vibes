package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillOfTheAbzan.class, EdgarMarkov.class, GrizzlyBears.class, HillGiant.class})
class WillOfTheAbzanTest extends BaseCardTest {

    @Test
    void sacrificesOnlyTheGreatestPowerCreatureAndLosesLife() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new HillGiant());

        castSingleMode(0, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void targetPlayerChoosesAmongTiedGreatestPowerCreaturesBeforeLifeLoss() {
        Permanent first = addCreatureReady(player2, new HillGiant());
        Permanent second = addCreatureReady(player2, new HillGiant());

        castSingleMode(0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
        harness.handlePermanentChosen(player2, first.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void returnsTargetCreatureFromOwnGraveyardToBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castSingleMode(1, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void commanderAllowsBothModes() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addCreatureReady(player2, new HillGiant());
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        addCreatureReady(player1, commander);

        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void cannotChooseBothModesWithoutCommander() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentWithoutCreaturesStillLosesLife() {
        castSingleMode(0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void firstModeCanChooseNoOpponents() {
        addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Will of the Abzan");
    }

    @Test
    void cannotTargetControllerWithFirstMode() {
        assertThatThrownBy(() -> castSingleMode(0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnCreatureFromOpponentsGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));

        assertThatThrownBy(() -> castSingleMode(1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotReturnNoncreatureFromOwnGraveyard() {
        Card sorcery = new WillOfTheAbzan();
        harness.setGraveyard(player1, List.of(sorcery));

        assertThatThrownBy(() -> castSingleMode(1, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commanderInCommandZoneDoesNotAllowBothModes() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void legendaryCreatureThatIsNotACommanderDoesNotAllowBothModes() {
        addCreatureReady(player1, new EdgarMarkov());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void controllingOpponentsCommanderAllowsBothModesWithNoOpponentTargets() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player2.getId(), List.of(commander));
        addCreatureReady(player1, commander);
        addCreatureReady(player2, new HillGiant());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(creature.getId()), null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertLife(player2, 20);
    }

    @Test
    void bothModesContinueWhenCommanderLeavesAfterCastingAndGraveyardTargetBecomesIllegal() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        Permanent permanent = addCreatureReady(player1, commander);
        addCreatureReady(player2, new HillGiant());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null);

        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerCommandZones.get(player1.getId()).add(commander);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void reanimationContinuesAfterOpponentChoosesTiedSacrifice() {
        Card commander = new EdgarMarkov();
        gd.playerCommanders.put(player1.getId(), List.of(commander));
        addCreatureReady(player1, commander);
        Permanent first = addCreatureReady(player2, new HillGiant());
        Permanent second = addCreatureReady(player2, new HillGiant());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), creature.getId()), null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player2, first.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
    }

    private void castSingleMode(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{mode}, List.of(targetId), null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
