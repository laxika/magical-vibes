package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecronDeathmark.class, Forest.class, GrizzlyBears.class})
class NecronDeathmarkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a creature and mills a player three cards")
    void etbDestroysCreatureAndMillsPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest firstMilledCard = new Forest();
        Forest secondMilledCard = new Forest();
        Forest thirdMilledCard = new Forest();
        harness.setLibrary(player2, List.of(firstMilledCard, secondMilledCard, thirdMilledCard));
        harness.setHand(player1, List.of(new NecronDeathmark()));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId(), creature.getId()));
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstMilledCard, secondMilledCard, thirdMilledCard);
    }

    @Test
    @DisplayName("ETB can decline the creature target and still mill the target player")
    void etbCanDeclineCreatureTarget() {
        Forest firstMilledCard = new Forest();
        Forest secondMilledCard = new Forest();
        Forest thirdMilledCard = new Forest();
        harness.setLibrary(player2, List.of(firstMilledCard, secondMilledCard, thirdMilledCard));
        harness.setHand(player1, List.of(new NecronDeathmark()));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Necron Deathmark");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(firstMilledCard, secondMilledCard, thirdMilledCard);
    }

    @Test
    @DisplayName("The ETB cannot target a noncreature permanent for destruction")
    void rejectsNonCreatureDestructionTarget() {
        Forest land = new Forest();
        harness.addToBattlefield(player2, land);
        harness.setHand(player1, List.of(new NecronDeathmark()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player2.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB can destroy a creature you control and mill yourself")
    void canTargetOwnCreatureAndSelf() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NecronDeathmark());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new NecronDeathmark()));
        addMana();

        harness.castCreature(player1, 0, List.of(player1.getId(), creature.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Necron Deathmark");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
    }

    @Test
    @DisplayName("ETB still mills when the creature target leaves before resolution")
    void millsWhenCreatureTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NecronDeathmark());
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setHand(player1, List.of(new NecronDeathmark()));
        addMana();

        harness.castCreature(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, third);
        harness.assertOnBattlefield(player1, "Necron Deathmark");
    }

    @Test
    @DisplayName("Flash permits casting during an opponent's upkeep and milling a short library")
    void flashDuringOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NecronDeathmark());
        Forest onlyCard = new Forest();
        harness.setLibrary(player2, List.of(onlyCard));
        harness.setHand(player1, List.of(new NecronDeathmark()));
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castCreature(player1, 0, List.of(player2.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Necron Deathmark");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(onlyCard);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
