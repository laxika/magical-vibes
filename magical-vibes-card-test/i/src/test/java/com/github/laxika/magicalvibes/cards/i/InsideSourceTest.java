package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InsideSource.class, GrizzlyBears.class})
class InsideSourceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 2/2 white and blue Detective token")
    void createsDetectiveToken() {
        castInsideSource();

        assertThat(countPermanents(player1, "Detective")).isEqualTo(1);
        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().getPower()).isEqualTo(2);
        assertThat(detective.getCard().getToughness()).isEqualTo(2);
        assertThat(detective.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getSubtypes()).contains(CardSubtype.DETECTIVE);
        assertThat(detective.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Boosts a Detective with +2/+0 and vigilance")
    void boostsDetective() {
        castInsideSource();
        Permanent source = findPermanent(player1, "Inside Source");
        source.setSummoningSick(false);
        Permanent detective = findPermanent(player1, "Detective");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, detective.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, detective, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a non-Detective creature")
    void cannotTargetNonDetective() {
        Permanent source = addCreatureReady(player1, new InsideSource());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castInsideSource() {
        harness.castFromHand(player1, new InsideSource(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void cannotTargetOpponentsDetective() {
        Permanent source = addCreatureReady(player1, new InsideSource());
        harness.enterBattlefieldAndReturn(player2, new InsideSource());
        resolveAllTriggers();
        Permanent detective = findPermanent(player2, "Detective");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        castInsideSource();
        Permanent detective = findPermanent(player1, "Detective");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        castInsideSource();
        findPermanent(player1, "Inside Source").setSummoningSick(false);
        Permanent detective = findPermanent(player1, "Detective");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        castInsideSource();
        findPermanent(player1, "Inside Source").setSummoningSick(false);
        Permanent detective = findPermanent(player1, "Detective");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithSpellOnStack() {
        castInsideSource();
        findPermanent(player1, "Inside Source").setSummoningSick(false);
        Permanent detective = findPermanent(player1, "Detective");
        harness.castFromHand(player1, new InsideSource(), "{2}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, detective.getId()))
                .isInstanceOf(IllegalStateException.class);
        resolveAllTriggers();
    }

    @Test
    void boostAndVigilanceExpireAtEndOfTurn() {
        castInsideSource();
        Permanent source = findPermanent(player1, "Inside Source");
        source.setSummoningSick(false);
        Permanent detective = findPermanent(player1, "Detective");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, detective.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, detective, Keyword.VIGILANCE)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, detective)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, detective)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, detective, Keyword.VIGILANCE)).isFalse();
    }
}
