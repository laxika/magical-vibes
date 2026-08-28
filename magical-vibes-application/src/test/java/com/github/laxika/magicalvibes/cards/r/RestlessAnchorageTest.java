package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({RestlessAnchorage.class})
class RestlessAnchorageTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Anchorage enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RestlessAnchorage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Restless Anchorage").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Restless Anchorage taps for white or blue mana")
    void tapsForWhiteOrBlueMana() {
        Permanent anchorage = addAnchorageReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);

        anchorage.untap();
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation makes Restless Anchorage a 2/3 white and blue Bird with flying")
    void animatesIntoFlyingBird() {
        Permanent anchorage = addAnchorageReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, anchorage)).isTrue();
        assertThat(gqs.isLand(gd, anchorage)).isTrue();
        assertThat(gqs.getEffectivePower(gd, anchorage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, anchorage)).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, anchorage))
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, anchorage)).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, anchorage, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The animation wears off at the end of the turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent anchorage = addAnchorageReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, anchorage)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, anchorage)).isFalse();
        assertThat(gqs.isLand(gd, anchorage)).isTrue();
    }

    @Test
    @DisplayName("Attacking with Restless Anchorage creates a Map token")
    void attackingCreatesMapToken() {
        Permanent anchorage = addAnchorageReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    private Permanent addAnchorageReady(Player player) {
        Permanent permanent = new Permanent(new RestlessAnchorage());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
