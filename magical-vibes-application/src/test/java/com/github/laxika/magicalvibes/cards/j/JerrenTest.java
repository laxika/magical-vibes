package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArrogantOutlaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrmendahlTheCorrupter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jerren.class, OrmendahlTheCorrupter.class, ArrogantOutlaw.class, GrizzlyBears.class})
class JerrenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by making its controller lose life and creating a Human token")
    void entersWithLifeLossAndHumanToken() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Jerren()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(humanTokenCount(player1)).isOne();
    }

    @Test
    @DisplayName("A nontoken Human death causes life loss and creates a Human token")
    void humanDeathTriggersAbility() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Jerren());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ArrogantOutlaw());

        human.setMarkedDamage(10);
        harness.runStateBasedActions();
        resolveAllStack();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(humanTokenCount(player1)).isOne();
    }

    @Test
    @DisplayName("Non-Humans and token Humans do not trigger the death ability")
    void nonHumanAndTokenDeathsDoNotTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Jerren());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        nonHuman.setMarkedDamage(10);
        harness.runStateBasedActions();
        resolveAllStack();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount(player1)).isZero();

        harness.setHand(player1, List.of(new Jerren()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        token.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllStack();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("At exactly 13 life, the controller may pay to transform")
    void transformsAtExactlyThirteenLifeAfterPayment() {
        Permanent jerren = addReadyJerren();
        harness.setLife(player1, 13);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jerren.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Ormendahl sacrifices another creature to draw a card")
    void ormendahlSacrificesAnotherCreatureToDraw() {
        Permanent ormendahl = addTransformedOrmendahl();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new ArrogantOutlaw()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, indexOf(player1, ormendahl), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The lifelink ability only targets a Human you control")
    void lifelinkAbilityRequiresHumanYouControl() {
        Permanent jerren = harness.addToBattlefieldAndReturn(player1, new Jerren());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ArrogantOutlaw());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, jerren), null, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, jerren), null, nonHuman.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyJerren() {
        Jerren card = new Jerren();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addTransformedOrmendahl() {
        Jerren card = new Jerren();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private long humanTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .count();
    }

    private void resolveAllStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
