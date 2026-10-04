package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.h.HungryGraffalon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Efflorescence.class, HungryGraffalon.class})
class EfflorescenceTest extends BaseCardTest {

    @Test
    @DisplayName("Without life gained, only adds two +1/+1 counters")
    void withoutLifeGainOnlyCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HungryGraffalon());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("If you gained life this turn, also grants trample and indestructible")
    void withLifeGainGrantsKeywords() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HungryGraffalon());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    void checksLifeGainAtResolutionAndCanTargetOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    void opponentsLifeGainDoesNotEnableInfusion() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    void keywordsExpireButCountersRemainAndPreviousTurnsLifeGainDoesNotCount() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HungryGraffalon());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
    }

    @Test
    void removedTargetDoesNotTransferEffectsToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new HungryGraffalon());
        harness.setHand(player1, List.of(new Efflorescence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE, Keyword.INDESTRUCTIBLE);
        harness.assertInGraveyard(player1, "Efflorescence");
        assertThat(gd.stack).isEmpty();
    }
}
