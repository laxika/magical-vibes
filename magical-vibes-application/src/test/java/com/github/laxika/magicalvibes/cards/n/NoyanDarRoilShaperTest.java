package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoyanDarRoilShaper.class, Divination.class, Forest.class, GrizzlyBears.class, Shock.class})
class NoyanDarRoilShaperTest extends BaseCardTest {

    @Test
    void acceptingTriggerAnimatesTargetLand() {
        harness.addToBattlefield(player1, new NoyanDarRoilShaper());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castShock();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownForest.getId())
                .doesNotContain(opponentForest.getId(), ownBear.getId());

        harness.handlePermanentChosen(player1, ownForest.getId());
        harness.passBothPriorities();

        assertThat(ownForest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, ownForest)).isTrue();
        assertThat(gqs.isLand(gd, ownForest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownForest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownForest)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, ownForest, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownForest, Keyword.HASTE)).isTrue();
        assertThat(ownForest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void decliningTriggerLeavesLandUnchanged() {
        harness.addToBattlefield(player1, new NoyanDarRoilShaper());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        castShock();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    void onlyInstantAndSorcerySpellsTrigger() {
        harness.addToBattlefield(player1, new NoyanDarRoilShaper());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    private void castShock() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
