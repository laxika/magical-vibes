package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestlessBivouac.class, GrizzlyBears.class})
class RestlessBivouacTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Bivouac enters tapped and produces red or white mana")
    void entersTappedAndProducesMana() {
        harness.setHand(player1, List.of(new RestlessBivouac()));
        harness.playLand(player1, 0);

        Permanent bivouac = findPermanent(player1, "Restless Bivouac");
        assertThat(bivouac.isTapped()).isTrue();

        bivouac.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Restless Bivouac becomes a 2/2 red and white Ox and stays a land")
    void animatesIntoOx() {
        Permanent bivouac = addReadyBivouac(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bivouac)).isTrue();
        assertThat(gqs.isLand(gd, bivouac)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bivouac)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bivouac)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, bivouac))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bivouac)).contains(CardSubtype.OX);
    }

    @Test
    @DisplayName("Restless Bivouac puts a +1/+1 counter on a creature you control when it attacks")
    void attackingPutsCounterOnTargetCreatureYouControl() {
        addReadyBivouac(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restless Bivouac cannot target an opponent's creature with its attack trigger")
    void attackTriggerCannotTargetOpponentCreature() {
        addReadyBivouac(player1);
        addAnimationMana(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restless Bivouac's animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent bivouac = addReadyBivouac(player1);
        addAnimationMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bivouac)).isFalse();
        assertThat(gqs.isLand(gd, bivouac)).isTrue();
    }

    private void addAnimationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }

    private Permanent addReadyBivouac(Player player) {
        Permanent permanent = new Permanent(new RestlessBivouac());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
