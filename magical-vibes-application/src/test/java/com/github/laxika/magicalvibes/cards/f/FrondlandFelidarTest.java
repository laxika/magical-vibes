package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TopanFreeblade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FrondlandFelidar.class, GrizzlyBears.class, FountainOfYouth.class, TopanFreeblade.class})
class FrondlandFelidarTest extends BaseCardTest {

    @Test
    void vigilantCreaturesYouControlCanTapTargetCreature() {
        Permanent felidar = addCreatureReady(player1, new FrondlandFelidar());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        activateGrantedAbility(felidar, target);

        assertThat(felidar.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void doesNotGrantAbilityToNonVigilantOrOpponentCreatures() {
        addCreatureReady(player1, new FrondlandFelidar());
        Permanent nonVigilant = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVigilant = addCreatureReady(player2, new TopanFreeblade());

        assertThat(gs.getEffectiveActivatedAbilities(gd, nonVigilant)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opponentVigilant)).isEmpty();
    }

    @Test
    void grantedAbilityCannotTargetNoncreaturePermanent() {
        Permanent felidar = addCreatureReady(player1, new FrondlandFelidar());
        Permanent noncreature = addCreatureReady(player2, new FountainOfYouth());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(felidar),
                0,
                null,
                noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
        assertThat(felidar.isTapped()).isFalse();
    }

    @Test
    void otherVigilantCreatureCanTapYourOwnCreature() {
        Permanent felidar = addCreatureReady(player1, new FrondlandFelidar());
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());

        activateGrantedAbility(freeblade, felidar);

        assertThat(freeblade.isTapped()).isTrue();
        assertThat(felidar.isTapped()).isTrue();
    }

    @Test
    void grantedAbilityRemainsOnStackAfterFelidarLeaves() {
        Permanent felidar = addCreatureReady(player1, new FrondlandFelidar());
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());
        Permanent target = addCreatureReady(player2, new FrondlandFelidar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(freeblade),
                0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(felidar);
        gd.playerGraveyards.get(player1.getId()).add(felidar.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, freeblade)).isEmpty();
    }

    @Test
    void summoningSickVigilantCreatureCannotPayTapCost() {
        addCreatureReady(player1, new FrondlandFelidar());
        Permanent freeblade = addCreatureReady(player1, new TopanFreeblade());
        freeblade.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new FrondlandFelidar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(freeblade),
                0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(freeblade.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantedAbilityRequiresOneMana() {
        Permanent felidar = addCreatureReady(player1, new FrondlandFelidar());
        Permanent target = addCreatureReady(player2, new FrondlandFelidar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(felidar),
                0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(felidar.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void activateGrantedAbility(Permanent source, Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(source),
                0,
                null,
                target.getId());
        harness.passBothPriorities();
    }

}
