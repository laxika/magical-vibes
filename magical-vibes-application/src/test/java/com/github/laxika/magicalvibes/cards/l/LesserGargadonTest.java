package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LesserGargadon.class, Mountain.class, GiantSpider.class})
class LesserGargadonTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Lesser Gargadon sacrifices its controller's only land")
    void attackingSacrificesLand() {
        addCreatureReady(player1, new LesserGargadon());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("Blocking with Lesser Gargadon sacrifices its controller's only land")
    void blockingSacrificesLand() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        addCreatureReady(player2, new LesserGargadon());
        harness.addToBattlefield(player2, new Mountain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("With multiple lands, controller chooses which one to sacrifice")
    void multipleLandsPromptChoice() {
        addCreatureReady(player1, new LesserGargadon());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent land = findPermanent(player1, "Mountain");
        harness.handleMultiplePermanentsChosen(player1, List.of(land.getId()));

        long lands = countPermanents(player1, "Mountain");
        assertThat(lands).isEqualTo(1);
    }

    @Test
    @DisplayName("With no lands, the attack trigger does nothing")
    void noLandsIsHarmless() {
        addCreatureReady(player1, new LesserGargadon());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lesser Gargadon");
    }

    @Test
    @DisplayName("Attacking sacrifices only the controller's land")
    void attackingOnlySacrificesControllerLand() {
        addCreatureReady(player1, new LesserGargadon());
        harness.addToBattlefield(player1, new Mountain());
        addCreatureReady(player1, new GiantSpider());
        harness.addToBattlefield(player2, new Mountain());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertOnBattlefield(player2, "Mountain");
    }
}
