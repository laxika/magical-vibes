package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfResurgence.class, Forest.class})
class WallOfResurgenceTest extends BaseCardTest {

    @Test
    void acceptingEtbAbilityPutsCountersOnTargetLandAndAnimatesIt() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(land.getPermanentAnimatedPower()).isEqualTo(0);
        assertThat(land.getPermanentAnimatedToughness()).isEqualTo(0);
        assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(land.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    void decliningEtbAbilityLeavesLandUnchanged() {
        Permanent land = addLand();

        castWall();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.isPermanentlyAnimated()).isFalse();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void onlyYourOwnLandCanBeTargeted() {
        addLand();
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        castWall();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addLand() {
        return harness.addToBattlefieldAndReturn(player1, new Forest());
    }

    private void castWall() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WallOfResurgence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
