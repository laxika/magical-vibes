package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PaladinClass.class, Opt.class})
class PaladinClassTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents' spells cost more during your turn")
    void taxesOpponentsSpellsDuringYourTurn() {
        harness.addToBattlefield(player1, new PaladinClass());
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        prepareForSorcery(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Level two boosts creatures you control")
    void levelTwoBoostsCreaturesYouControl() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        Permanent creature = addReadyCreature(player1);

        levelUpToTwo(paladinClass);

        assertThat(paladinClass.getCounterCount(CounterType.LEVEL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Level three boosts one attacking creature for each other attacker and grants double strike")
    void levelThreeBoostsForOtherAttackersAndGrantsDoubleStrike() {
        Permanent paladinClass = harness.addToBattlefieldAndReturn(player1, new PaladinClass());
        Permanent target = addReadyCreature(player1);
        Permanent otherAttacker = addReadyCreature(player1);
        Permanent anotherAttacker = addReadyCreature(player1);

        levelUpToTwo(paladinClass);
        levelUpToThree(paladinClass);

        int powerBeforeAttack = gqs.getEffectivePower(gd, target);
        int toughnessBeforeAttack = gqs.getEffectiveToughness(gd, target);
        declareAttackers(List.of(1, 2, 3));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(powerBeforeAttack + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(toughnessBeforeAttack + 2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, anotherAttacker)).isEqualTo(3);
    }

    private void levelUpToTwo(Permanent paladinClass) {
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, battlefieldIndex(paladinClass), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent paladinClass) {
        prepareForSorcery(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, battlefieldIndex(paladinClass), 1, null, null);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareForSorcery(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Card creature = new Card();
        creature.setName("Test Creature");
        creature.setType(CardType.CREATURE);
        creature.setPower(2);
        creature.setToughness(2);
        return addReadyPermanent(player, creature);
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
