package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CycloneSire.class, Forest.class, Shock.class, Solemnity.class})
class CycloneSireTest extends BaseCardTest {

    @Test
    void acceptingDeathTriggerAnimatesTargetLand() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new CycloneSire());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        destroySire(sire.getId());

        chooseDeathTriggerTarget(land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(land.getGrantedKeywords()).contains(Keyword.HASTE);
        assertThat(gqs.getEffectiveCardTypes(gd, land)).contains(CardType.LAND, CardType.CREATURE);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    void decliningDeathTriggerLeavesLandUnchanged() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new CycloneSire());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        destroySire(sire.getId());

        chooseDeathTriggerTarget(land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.isPermanentlyAnimated()).isFalse();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void deathTriggerOnlyOffersLandsYouControl() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new CycloneSire());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        destroySire(sire.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownLand.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseCounterPlacementWhenSolemnityPreventsIt() {
        harness.addToBattlefield(player2, new Solemnity());
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new CycloneSire());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        destroySire(sire.getId());

        chooseDeathTriggerTarget(land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.findPermanentById(gd, land.getId())).isSameAs(land);
    }

    @Test
    void animationAndHastePersistAfterTurnEnds() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new CycloneSire());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        destroySire(sire.getId());

        chooseDeathTriggerTarget(land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void addsCountersToLandWithExistingCounters() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new CycloneSire());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        destroySire(sire.getId());

        chooseDeathTriggerTarget(land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(5);
    }

    @Test
    void targetThatChangesControllersBeforeResolutionIsIllegal() {
        Permanent sire = harness.addToBattlefieldAndReturn(player1, new CycloneSire());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        destroySire(sire.getId());
        chooseDeathTriggerTarget(land.getId());

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    private void destroySire(UUID sireId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        gqs.findPermanentById(gd, sireId).setMarkedDamage(2);
        harness.castAndResolveInstant(player2, 0, sireId);
    }

    private void chooseDeathTriggerTarget(UUID landId) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, landId);
    }
}
