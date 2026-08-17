package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornetQueen;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AatchikEmeraldRadianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one Insect token for each artifact or creature card in controller's graveyard")
    void createsInsectsForArtifactOrCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(
                new DarksteelRelic(),
                new GrizzlyBears(),
                new Ornithopter(),
                new Shock()));

        castAatchik();

        assertThat(insectTokens(player1)).hasSize(3);
    }

    @Test
    @DisplayName("An Insect you control dying puts a counter on Aatchik and makes each opponent lose 1 life")
    void triggersForAllyInsectDeath() {
        harness.setLife(player2, 20);
        Permanent aatchik = harness.addToBattlefieldAndReturn(player1, new AatchikEmeraldRadian());
        harness.addToBattlefield(player1, new HornetQueen());

        killWithShock(player1, player1, "Hornet Queen");

        assertThat(aatchik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A non-Insect creature dying does not trigger Aatchik")
    void doesNotTriggerForAllyNonInsectDeath() {
        harness.setLife(player2, 20);
        Permanent aatchik = harness.addToBattlefieldAndReturn(player1, new AatchikEmeraldRadian());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player1, player1, "Grizzly Bears");

        assertThat(aatchik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opponent's Insect dying does not trigger Aatchik")
    void doesNotTriggerForOpponentInsectDeath() {
        harness.setLife(player2, 20);
        Permanent aatchik = harness.addToBattlefieldAndReturn(player1, new AatchikEmeraldRadian());
        harness.addToBattlefield(player2, new HornetQueen());

        killWithShock(player1, player2, "Hornet Queen");

        assertThat(aatchik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    private void castAatchik() {
        harness.setHand(player1, List.of(new AatchikEmeraldRadian()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> insectTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Insect"))
                .toList();
    }
}
