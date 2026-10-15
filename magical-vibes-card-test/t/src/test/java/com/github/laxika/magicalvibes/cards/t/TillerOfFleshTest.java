package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OzolithTheShatteredSpire;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TillerOfFlesh.class, GrizzlyBears.class, Shock.class, TandemTakedown.class, OzolithTheShatteredSpire.class})
class TillerOfFleshTest extends BaseCardTest {

    @Test
    void incubatesWhenYouCastASpellThatTargetsAPermanent() {
        harness.addToBattlefield(player1, new TillerOfFlesh());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotIncubateWhenYouCastASpellThatTargetsAPlayer() {
        harness.addToBattlefield(player1, new TillerOfFlesh());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(findPermanents(player1, "Incubator")).isEmpty();
    }

    @Test
    void incubatesOnlyOnceForASpellWithMultiplePermanentTargets() {
        Permanent tiller = harness.addToBattlefieldAndReturn(player1, new TillerOfFlesh());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TillerOfFlesh());
        harness.setHand(player1, List.of(new TandemTakedown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), tiller.getId()));

        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player2, "Incubator")).isEmpty();
    }

    @Test
    void doesNotIncubateForOpponentsTargetedSpell() {
        Permanent tiller = harness.addToBattlefieldAndReturn(player1, new TillerOfFlesh());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, tiller.getId());

        assertThat(findPermanents(player1, "Incubator")).isEmpty();
    }

    @Test
    void doesNotIncubateForASpellWithoutTargets() {
        harness.addToBattlefield(player1, new TillerOfFlesh());
        harness.setHand(player1, List.of(new TillerOfFlesh()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).isEmpty();
    }

    @Test
    void incubatorTransformsIntoAnArtifactCreatureAndKeepsItsCounters() {
        Permanent tiller = harness.addToBattlefieldAndReturn(player1, new TillerOfFlesh());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, tiller.getId());
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(incubator.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(incubator.getCard().getSubtypes()).contains(CardSubtype.INCUBATOR);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(incubator.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(incubator.getCard().getSubtypes()).containsExactly(CardSubtype.PHYREXIAN);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
        assertThat(countPermanents(player1, "Phyrexian Token")).isEqualTo(1);
        assertThat(findPermanents(player1, "Incubator")).isEmpty();
    }

    @Test
    void stillIncubatesIfTheSpellTargetLeavesBeforeTheTriggerResolves() {
        harness.addToBattlefield(player1, new TillerOfFlesh());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TillerOfFlesh());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void incubatorEntersWithAnAdditionalCounterFromOzolith() {
        Permanent tiller = harness.addToBattlefieldAndReturn(player1, new TillerOfFlesh());
        harness.addToBattlefield(player1, new OzolithTheShatteredSpire());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TillerOfFlesh());
        harness.setHand(player1, List.of(new TandemTakedown()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId(), tiller.getId()));

        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
