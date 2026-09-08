package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TopanFreeblade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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
        Permanent noncreature = addReadyPermanent(player2, new FountainOfYouth());

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

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
