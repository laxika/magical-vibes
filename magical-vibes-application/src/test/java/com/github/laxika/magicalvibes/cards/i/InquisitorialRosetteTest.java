package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LightshieldArray;
import com.github.laxika.magicalvibes.cards.s.SicarianInfiltrator;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({InquisitorialRosette.class, SicarianInfiltrator.class,
        InvasionOfGobakhan.class, LightshieldArray.class})
class InquisitorialRosetteTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the equipped creature creates an attacking Astartes Warrior and grants menace")
    void attackTriggerCreatesAttackingTokenAndGrantsMenace() {
        Permanent creature = addCreatureReady(player1);
        Permanent rosette = addRosette(player1);
        rosette.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent token = findPermanent(player1, "Astartes Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ASTARTES, CardSubtype.WARRIOR);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(countPermanents(player1, "Astartes Warrior")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Menace granted by the attack trigger wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1);
        Permanent rosette = addRosette(player1);
        rosette.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An unattached Equipment does not trigger when a creature attacks")
    void unattachedEquipmentDoesNotTrigger() {
        addCreatureReady(player1);
        addRosette(player1);

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Astartes Warrior"));
    }

    @Test
    @DisplayName("All attacking creatures gain menace, but nonattacking creatures do not")
    void menaceAppliesOnlyToCreaturesAttackingAtResolution() {
        Permanent equipped = addCreatureReady(player1);
        Permanent otherAttacker = addCreatureReady(player1);
        Permanent nonattacker = addCreatureReady(player1);
        Permanent defender = addCreatureReady(player2);
        addRosette(player1).setAttachedTo(equipped.getId());

        declareAttackers(List.of(0, 1));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.hasKeyword(gd, equipped, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Astartes Warrior"), Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, defender, Keyword.MENACE)).isFalse();

        otherAttacker.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.MENACE)).isTrue();
        nonattacker.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger the attached Equipment")
    void unequippedAttackerDoesNotTrigger() {
        Permanent equipped = addCreatureReady(player1);
        addCreatureReady(player1);
        addRosette(player1).setAttachedTo(equipped.getId());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Astartes Warrior")).isZero();
    }

    @Test
    @DisplayName("The attack trigger resolves after its Equipment leaves the battlefield")
    void triggerSurvivesEquipmentLeavingBattlefield() {
        Permanent creature = addCreatureReady(player1);
        Permanent rosette = addRosette(player1);
        rosette.setAttachedTo(creature.getId());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(rosette);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(countPermanents(player1, "Astartes Warrior")).isEqualTo(1);
        assertThat(findPermanent(player1, "Astartes Warrior").isAttacking()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("The Equipment controller creates the token when an opponent's equipped creature attacks")
    void opponentsEquippedCreatureCreatesNonattackingTokenForEquipmentController() {
        Permanent creature = addCreatureReady(player2);
        addRosette(player1).setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent token = findPermanent(player1, "Astartes Warrior");
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(countPermanents(player2, "Astartes Warrior")).isZero();
    }

    @Test
    @DisplayName("Equip costs three generic mana and attaches on resolution")
    void equipPaysThreeManaAndAttaches() {
        Permanent rosette = addRosette(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(rosette.getAttachedTo()).isNull();
        harness.passBothPriorities();
        assertThat(rosette.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void equipCannotTargetOpponentCreature() {
        addRosette(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipIsSorcerySpeed() {
        addRosette(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The created token can attack a battle protected by the defending player")
    void tokenCanAttackBattleInsteadOfEquippedCreaturesDefender() {
        Permanent creature = addCreatureReady(player1);
        addRosette(player1).setAttachedTo(creature.getId());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfGobakhan());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, battle.getId()));

        Permanent token = findPermanent(player1, "Astartes Warrior");
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(battle.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new SicarianInfiltrator());
    }

    private Permanent addRosette(Player player) {
        return harness.addToBattlefieldAndReturn(player, new InquisitorialRosette());
    }
}
