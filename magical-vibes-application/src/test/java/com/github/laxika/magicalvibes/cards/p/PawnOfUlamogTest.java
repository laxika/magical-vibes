package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ConsumeTheMeek;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PawnOfUlamog.class, GrizzlyBears.class, Shock.class, ConsumeTheMeek.class, NestInvader.class})
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
        harness.enterBattlefieldAndReturn(player1, new NestInvader());
        harness.passBothPriorities();

        killWithShock(player2, player1, "Eldrazi Spawn");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerWhenOpponentsCreatureDies() {
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    void tokenCopyOfPawnStillTriggersForItsOwnDeath() {
        PawnOfUlamog tokenCopy = new PawnOfUlamog();
        tokenCopy.setToken(true);
        harness.addToBattlefield(player1, tokenCopy);

        killWithShock(player2, player1, "Pawn of Ulamog");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachPawnTriggersForItselfAndTheOtherWhenBothDieTogether() {
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player1, new PawnOfUlamog());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new ConsumeTheMeek(), "{3}{B}{B}");
        harness.passBothPriorities();

        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction())
                    .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(findPermanents(player1, "Pawn of Ulamog")).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(4);
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.castAndResolveInstant(caster, 0, targetId);
    }

}
