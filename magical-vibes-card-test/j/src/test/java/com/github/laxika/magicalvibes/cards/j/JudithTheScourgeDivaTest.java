package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JudithTheScourgeDiva.class, GrizzlyBears.class, FlameJavelin.class})
class JudithTheScourgeDivaTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+0")
    void buffsOtherCreaturesYouControl() {
        Permanent judith = harness.addToBattlefieldAndReturn(player1, new JudithTheScourgeDiva());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, judith)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A nontoken ally creature dying deals 1 damage to any target")
    void allyNontokenDeathDealsDamage() {
        harness.addToBattlefield(player1, new JudithTheScourgeDiva());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithFlameJavelin(bear.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Judith's own nontoken death triggers the damage ability")
    void ownNontokenDeathDealsDamage() {
        Permanent judith = harness.addToBattlefieldAndReturn(player1, new JudithTheScourgeDiva());

        killWithFlameJavelin(judith.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A token creature dying does not trigger the damage ability")
    void tokenDeathDoesNotDealDamage() {
        harness.addToBattlefield(player1, new JudithTheScourgeDiva());
        Card tokenCard = new Card();
        tokenCard.setName("Saproling");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);

        token.setMarkedDamage(1);
        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An opposing creature dying does not trigger Judith")
    void opposingCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new JudithTheScourgeDiva());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithFlameJavelin(bear.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The death trigger can damage a creature")
    void deathTriggerDamagesCreature() {
        harness.addToBattlefield(player1, new JudithTheScourgeDiva());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithFlameJavelin(ally.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Judith sees her own death and an ally's simultaneous death")
    void simultaneousDeathsTriggerSeparately() {
        Permanent judith = harness.addToBattlefieldAndReturn(player1, new JudithTheScourgeDiva());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        judith.setMarkedDamage(2);
        ally.setMarkedDamage(2);

        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A token copy of Judith does not trigger for its own death")
    void tokenJudithDoesNotTriggerForOwnDeath() {
        JudithTheScourgeDiva tokenCard = new JudithTheScourgeDiva();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        token.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private void killWithFlameJavelin(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
