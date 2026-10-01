package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArgothianUprooting.class, Forest.class, GrizzlyBears.class})
class ArgothianUprootingTest extends BaseCardTest {

    @Test
    void animatesExactlyXControlledLands() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ArgothianUprooting()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2, List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        for (Permanent land : List.of(firstLand, secondLand)) {
            assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
            assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
            assertThat(land.getGrantedKeywords()).contains(Keyword.REACH, Keyword.HASTE);
            assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        }
    }

    @Test
    void leavingLandConjuresTappedForest() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ArgothianUprooting()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 1, land.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .anySatisfy(forest -> assertThat(forest.isTapped()).isTrue());
    }

    @Test
    void onlyTargetsLandsYouControl() {
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArgothianUprooting()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new ArgothianUprooting()));
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
