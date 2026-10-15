package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.p.PlagueMyr;
import com.github.laxika.magicalvibes.cards.s.SerumRaker;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InkmothNexus.class, SerumRaker.class, PlagueMyr.class})
class InkmothNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Animation cannot be activated without paying one mana")
    void animationRequiresMana() {
        addCreatureReady(player1, new InkmothNexus());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Tapping Inkmoth Nexus produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(nexus);

        harness.tapPermanent(player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isSameAs(nexus.getCard());
        assertThat(entry.getTargetId()).isEqualTo(nexus.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 1/1 artifact creature with flying and infect")
    void resolvingAbilityMakesItAnimated() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(nexus.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(nexus.getAnimatedPower()).isEqualTo(1);
        assertThat(nexus.getAnimatedToughness()).isEqualTo(1);
        assertThat(gqs.isCreature(gd, nexus)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nexus)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nexus)).isEqualTo(1);
        assertThat(nexus.getAnimatedColor()).isNull();
        assertThat(nexus.getTransientSubtypes()).containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.BLINKMOTH);
        assertThat(nexus.getGrantedKeywords()).containsExactlyInAnyOrder(Keyword.FLYING, Keyword.INFECT);
        assertThat(gqs.isArtifact(nexus)).isTrue();
    }

    @Test
    @DisplayName("Inkmoth Nexus is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, nexus)).isTrue();
        assertThat(gqs.isCreature(gd, nexus)).isTrue();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(nexus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, nexus)).isTrue();
        assertThat(gqs.isArtifact(nexus)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nexus.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, nexus)).isFalse();
        assertThat(gqs.isArtifact(nexus)).isFalse();
        assertThat(nexus.getGrantedKeywords()).isEmpty();
        assertThat(nexus.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Inkmoth Nexus is not a creature or artifact before activation")
    void notACreatureOrArtifactBeforeActivation() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());

        assertThat(gqs.isCreature(gd, nexus)).isFalse();
        assertThat(gqs.isLand(gd, nexus)).isTrue();
        assertThat(gqs.isArtifact(nexus)).isFalse();
    }

    @Test
    @DisplayName("An animated Nexus deals poison instead of life loss")
    void unblockedDamageGivesPoison() {
        addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("An animated Nexus gives a flying blocker a minus counter")
    void blockedDamageGivesMinusCounter() {
        addCreatureReady(player1, new InkmothNexus());
        Permanent blocker = addCreatureReady(player2, new SerumRaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Inkmoth Nexus");
        harness.assertOnBattlefield(player2, "Serum Raker");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block an animated Nexus")
    void flyingPreventsGroundBlocker() {
        addCreatureReady(player1, new InkmothNexus());
        addCreatureReady(player2, new PlagueMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A newly entered Nexus can animate but cannot tap for mana as a creature")
    void newlyEnteredAnimatedNexusCannotTapForMana() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, nexus)).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(nexus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An animated Nexus retains its mana ability")
    void animatedNexusCanTapForMana() {
        Permanent nexus = addCreatureReady(player1, new InkmothNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.tapPermanent(player1, 0);

        assertThat(nexus.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
