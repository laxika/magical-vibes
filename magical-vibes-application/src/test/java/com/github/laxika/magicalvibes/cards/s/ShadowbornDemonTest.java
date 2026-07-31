package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AwokenDemon;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShadowbornDemonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target non-Demon creature")
    void etbDestroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShadowbornDemon()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a Demon")
    void cannotTargetDemon() {
        harness.addToBattlefield(player2, new AwokenDemon());
        harness.setHand(player1, List.of(new ShadowbornDemon()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        UUID targetId = harness.getPermanentId(player2, "Awoken Demon");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Demon creature");
    }

    @Test
    @DisplayName("Upkeep: with fewer than six creature cards in the graveyard, controller sacrifices a creature")
    void upkeepSacrificesWithSmallGraveyard() {
        harness.addToBattlefieldAndReturn(player1, new ShadowbornDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GiantSpider()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertOnBattlefield(player1, "Shadowborn Demon");
    }

    @Test
    @DisplayName("Upkeep: it may sacrifice itself when it is the only creature")
    void upkeepCanSacrificeItself() {
        harness.addToBattlefieldAndReturn(player1, new ShadowbornDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Shadowborn Demon");
        harness.assertInGraveyard(player1, "Shadowborn Demon");
    }

    @Test
    @DisplayName("Upkeep: no sacrifice with six creature cards in the graveyard")
    void upkeepNoSacrificeWithSixCreatureCards() {
        harness.addToBattlefieldAndReturn(player1, new ShadowbornDemon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GiantSpider(), new GiantSpider(), new GiantSpider(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new ShadowbornDemon());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shadowborn Demon");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
