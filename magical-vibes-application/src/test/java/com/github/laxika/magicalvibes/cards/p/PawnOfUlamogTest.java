package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PawnOfUlamogTest extends BaseCardTest {

    @Test
    void createsSpawnWhenAnotherNontokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> spawns = findPermanents(player1, "Eldrazi Spawn");
        assertThat(spawns).hasSize(1);
        assertThat(spawns.getFirst().getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(spawns.getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);

        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawns.getFirst());
        harness.activateAbility(player1, spawnIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    void createsSpawnWhenPawnOfUlamogDies() {
        harness.addToBattlefield(player1, new PawnOfUlamog());

        killWithShock(player2, player1, "Pawn of Ulamog");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    void mayDeclineToCreateSpawn() {
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    void doesNotTriggerWhenTokenCreatureYouControlDies() {
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player1, new SpawnToken());

        killWithShock(player2, player1, "Spawn Token");

        assertThat(gd.stack).isEmpty();
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
    }

    private static class SpawnToken extends com.github.laxika.magicalvibes.model.Card {

        private SpawnToken() {
            setName("Spawn Token");
            setType(CardType.CREATURE);
            setManaCost("");
            setPower(0);
            setToughness(1);
            setToken(true);
        }
    }
}
