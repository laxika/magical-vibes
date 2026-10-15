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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AatchikEmeraldRadian.class, DarksteelRelic.class, GrizzlyBears.class,
        HornetQueen.class, Ornithopter.class, Shock.class})
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

    @Test
    @DisplayName("ETB ignores the opponent's graveyard and creates no tokens for an empty controller graveyard")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new AatchikEmeraldRadian()));

        castAatchik();

        assertThat(insectTokens(player1)).isEmpty();
        assertThat(insectTokens(player2)).isEmpty();
    }

    @Test
    @DisplayName("ETB counts the graveyard when the trigger resolves")
    void countsGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of());
        prepareAatchik();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new AatchikEmeraldRadian()));

        harness.passBothPriorities();

        assertThat(insectTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Each Insect token death triggers independently and does not drain the controller")
    void triggersForEachInsectTokenDeath() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new AatchikEmeraldRadian(), new AatchikEmeraldRadian()));
        castAatchik();
        Permanent aatchik = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof AatchikEmeraldRadian)
                .findFirst().orElseThrow();

        killWithShock(player1, player1, "Insect");
        killWithShock(player1, player1, "Insect");

        assertThat(insectTokens(player1)).isEmpty();
        assertThat(aatchik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Aatchik's own death does not trigger its ability")
    void doesNotTriggerForItsOwnDeath() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AatchikEmeraldRadian());

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        UUID aatchikId = harness.getPermanentId(player1, "Aatchik, Emerald Radian");
        harness.castAndResolveInstant(player1, 0, aatchikId);
        harness.castAndResolveInstant(player1, 0, aatchikId);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AatchikEmeraldRadian);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The death trigger still drains the opponent after Aatchik leaves the battlefield")
    void drainsAfterSourceDiesInResponse() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new AatchikEmeraldRadian()));
        castAatchik();
        UUID aatchikId = harness.getPermanentId(player1, "Aatchik, Emerald Radian");
        UUID insectId = insectTokens(player1).getFirst().getId();
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, aatchikId);
        harness.castAndResolveInstant(player1, 0, insectId);
        harness.castAndResolveInstant(player1, 0, aatchikId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    private void prepareAatchik() {
        harness.setHand(player1, List.of(new AatchikEmeraldRadian()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void castAatchik() {
        prepareAatchik();
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
        harness.castAndResolveInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private List<Permanent> insectTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> p.getCard().getName().equals("Insect"))
                .toList();
    }
}
