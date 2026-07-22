package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestlessBloodseekerTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Blood token at your end step if you gained life this turn")
    void createsBloodTokenWhenLifeGained() {
        harness.addToBattlefield(player1, new RestlessBloodseeker());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bloodTokenCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates no Blood token at end step without life gain")
    void noTokenWithoutLifeGain() {
        harness.addToBattlefield(player1, new RestlessBloodseeker());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bloodTokenCount(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new RestlessBloodseeker());
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(bloodTokenCount(player1)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing two Blood tokens transforms into Bloodsoaked Reveler")
    void sacrificeTwoBloodTransforms() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new RestlessBloodseeker());
        addBloodToken(player1);
        addBloodToken(player1);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, seeker), null, null);
        harness.passBothPriorities();

        assertThat(seeker.isTransformed()).isTrue();
        assertThat(seeker.getCard().getName()).isEqualTo("Bloodsoaked Reveler");
        assertThat(gqs.getEffectivePower(gd, seeker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, seeker)).isEqualTo(3);
        assertThat(bloodTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Cannot transform without two Blood tokens")
    void cannotTransformWithoutTwoBlood() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new RestlessBloodseeker());
        addBloodToken(player1);
        forceMainPhase(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, seeker), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(seeker.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Back face {4}{B} drains opponent for 2 and gains 2 life")
    void backFaceDrainAbility() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new RestlessBloodseeker());
        addBloodToken(player1);
        addBloodToken(player1);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, seeker), null, null);
        harness.passBothPriorities();

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int oppBefore = gd.playerLifeTotals.get(player2.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, indexOf(player1, seeker), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(oppBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Transformed face still creates Blood at end step after life gain")
    void backFaceCreatesBloodAtEndStep() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new RestlessBloodseeker());
        addBloodToken(player1);
        addBloodToken(player1);
        forceMainPhase(player1);

        harness.activateAbility(player1, indexOf(player1, seeker), null, null);
        harness.passBothPriorities();

        gd.lifeGainedThisTurn.put(player1.getId(), 1);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(bloodTokenCount(player1)).isEqualTo(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void forceMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private long bloodTokenCount(Player owner) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blood"))
                .count();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void addBloodToken(Player player) {
        Card bloodCard = new Card();
        bloodCard.setName("Blood");
        bloodCard.setType(CardType.ARTIFACT);
        bloodCard.setManaCost("");
        bloodCard.setToken(true);
        bloodCard.setColor(null);
        bloodCard.setSubtypes(List.of(CardSubtype.BLOOD));
        bloodCard.addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DiscardCardTypeCost(null, null), new SacrificeSelfCost(), new DrawCardEffect()),
                "{1}, {T}, Discard a card, Sacrifice this token: Draw a card."
        ));
        Permanent blood = new Permanent(bloodCard);
        blood.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(blood);
    }
}
