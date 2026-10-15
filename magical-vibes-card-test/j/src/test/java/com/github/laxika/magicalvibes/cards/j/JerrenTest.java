package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArrogantOutlaw;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.o.OrmendahlTheCorrupter;
import com.github.laxika.magicalvibes.cards.u.UnrulyMob;
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

@CardUsed({Jerren.class, OrmendahlTheCorrupter.class, ArrogantOutlaw.class, UnrulyMob.class, Frogify.class})
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
        Permanent human = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());

        human.setMarkedDamage(10);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(humanTokenCount(player1)).isOne();
    }

    @Test
    @DisplayName("Non-Humans and token Humans do not trigger the death ability")
    void nonHumanAndTokenDeathsDoNotTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Jerren());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new ArrogantOutlaw());

        nonHuman.setMarkedDamage(10);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount(player1)).isZero();

        Permanent human = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        human.setMarkedDamage(10);
        harness.runStateBasedActions();
        resolveAllTriggers();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        harness.setLife(player1, 20);
        token.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("At exactly 13 life, the controller may pay to transform")
    void transformsAtExactlyThirteenLifeAfterPayment() {
        Permanent jerren = addReadyJerren();
        harness.setLife(player1, 13);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(jerren.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Ormendahl sacrifices another creature to draw a card")
    void ormendahlSacrificesAnotherCreatureToDraw() {
        Permanent ormendahl = addTransformedOrmendahl();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArrogantOutlaw());
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
        Permanent human = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new ArrogantOutlaw());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, jerren), null, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.LIFELINK)).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, jerren), null, nonHuman.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void formerHumanDeathDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Jerren());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, human.getId());
        harness.passBothPriorities();
        assertThat(gqs.effectiveCreatureSubtypes(gd, human)).containsExactly(CardSubtype.FROG);

        human.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(human.getOriginalCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    void ownDeathDoesNotTrigger() {
        harness.setLife(player1, 20);
        Permanent jerren = addReadyJerren();
        jerren.setMarkedDamage(3);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    void simultaneousDeathWithAnotherHumanStillTriggers() {
        harness.setLife(player1, 20);
        Permanent jerren = addReadyJerren();
        Permanent human = harness.addToBattlefieldAndReturn(player1, new UnrulyMob());
        jerren.setMarkedDamage(3);
        human.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(jerren.getOriginalCard(), human.getOriginalCard());
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(humanTokenCount(player1)).isOne();
    }

    @Test
    void opponentsHumanDeathDoesNotTrigger() {
        harness.setLife(player1, 20);
        addReadyJerren();
        Permanent human = harness.addToBattlefieldAndReturn(player2, new UnrulyMob());
        human.setMarkedDamage(1);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(humanTokenCount(player1)).isZero();
    }

    @Test
    void mayDeclineTransformation() {
        Permanent jerren = addReadyJerren();
        harness.setLife(player1, 13);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(jerren.isTransformed()).isFalse();
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
    }

    @Test
    void lifeBelowThirteenDoesNotTriggerTransformation() {
        Permanent jerren = addReadyJerren();
        harness.setLife(player1, 12);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(jerren.isTransformed()).isFalse();
    }

    @Test
    void lifeAboveThirteenDoesNotTriggerTransformation() {
        Permanent jerren = addReadyJerren();
        harness.setLife(player1, 14);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(jerren.isTransformed()).isFalse();
    }

    @Test
    void transformationRechecksLifeOnResolution() {
        Permanent jerren = addReadyJerren();
        harness.setLife(player1, 13);
        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 14);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(jerren.isTransformed()).isFalse();
    }

    @Test
    void opponentsEndStepDoesNotTriggerTransformation() {
        Permanent jerren = addReadyJerren();
        harness.setLife(player1, 13);
        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(jerren.isTransformed()).isFalse();
    }

    @Test
    void lifelinkCannotTargetOpponentsHuman() {
        Permanent jerren = addReadyJerren();
        Permanent human = harness.addToBattlefieldAndReturn(player2, new UnrulyMob());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, jerren), null, human.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ormendahlCannotSacrificeItself() {
        Permanent ormendahl = addTransformedOrmendahl();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, ormendahl), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ormendahl);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyJerren() {
        return addCreatureReady(player1, new Jerren());
    }

    private Permanent addTransformedOrmendahl() {
        Permanent permanent = addReadyJerren();
        permanent.setCard(permanent.getCard().getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private long humanTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .count();
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
