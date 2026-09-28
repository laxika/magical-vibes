package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JirinaKudro.class, EliteVanguard.class, GrizzlyBears.class})
class JirinaKudroTest extends BaseCardTest {

    @Test
    void createsHumanSoldiersForCommanderCastsAndBoostsOtherHumans() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Card commander = addCommanderToCommandZone();
        castCommander(commander, 1);
        gd.stack.clear();
        gd.priorityPassedBy.clear();
        gd.playerCommandZones.get(player1.getId()).add(commander);
        castCommander(commander, 3);
        gd.stack.clear();
        gd.priorityPassedBy.clear();

        Permanent jirina = harness.enterBattlefieldAndReturn(player1, new JirinaKudro());
        Permanent human = addCreatureReady(player1, new EliteVanguard());
        Permanent nonHuman = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentHuman = addCreatureReady(player2, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, jirina)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jirina)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, nonHuman)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentHuman)).isEqualTo(2);

        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Human Soldier");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    private Card addCommanderToCommandZone() {
        Card commander = new Card();
        commander.setName("Test Commander");
        commander.setType(CardType.CREATURE);
        commander.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        commander.setManaCost("{1}");
        commander.setPower(2);
        commander.setToughness(2);
        commander.setOwnerId(player1.getId());
        commander.freeze();
        gd.format = com.github.laxika.magicalvibes.model.DeckFormat.COMMANDER;
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(commander)));
        return commander;
    }

    private void castCommander(Card commander, int mana) {
        harness.addMana(player1, ManaColor.COLORLESS, mana);
        gs.castCommander(gd, player1, commander.getId(),
                () -> gs.playCard(gd, player1, 0, null, null, null));
    }
}
