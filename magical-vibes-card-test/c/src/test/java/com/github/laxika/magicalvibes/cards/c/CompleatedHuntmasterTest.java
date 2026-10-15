package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CompleatedHuntmaster.class, GrizzlyBears.class, Spellbook.class})
class CompleatedHuntmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature incubates three")
    void sacrificesCreatureAndIncubatesThree() {
        Permanent huntmaster = addCreatureReady(player1, new CompleatedHuntmaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(huntmaster), null, null);
        harness.passBothPriorities();

        assertThat(huntmaster.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing another artifact incubates three")
    void sacrificesArtifactAndIncubatesThree() {
        Permanent huntmaster = addCreatureReady(player1, new CompleatedHuntmaster());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(huntmaster), null, null);
        harness.passBothPriorities();

        assertThat(huntmaster.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(artifact.getCard());
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot sacrifice the Huntmaster itself")
    void requiresAnotherCreatureOrArtifact() {
        Permanent huntmaster = addCreatureReady(player1, new CompleatedHuntmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(huntmaster), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void incubatorHasCorrectFrontFace() {
        Permanent huntmaster = addCreatureReady(player1, new CompleatedHuntmaster());
        harness.addToBattlefield(player1, new CompleatedHuntmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(huntmaster), null, null);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(countPermanents(player1, "Incubator")).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(incubator.isTapped()).isFalse();
        assertThat(incubator.getCard().getSubtypes()).extracting(Enum::name).contains("INCUBATOR");
    }

    @Test
    void incubatorTransformsImmediatelyForTwoManaAndKeepsCounters() {
        Permanent huntmaster = addCreatureReady(player1, new CompleatedHuntmaster());
        harness.addToBattlefield(player1, new CompleatedHuntmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(huntmaster), null, null);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
        assertThat(incubator.getCard().getSubtypes()).extracting(Enum::name).contains("PHYREXIAN");
        assertThat(incubator.isTapped()).isFalse();
    }

    @Test
    void transformedIncubatorHasItsRequiredName() {
        Permanent huntmaster = addCreatureReady(player1, new CompleatedHuntmaster());
        harness.addToBattlefield(player1, new CompleatedHuntmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(huntmaster), null, null);
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
    }

    @Test
    void cannotSacrificeAnOpponentsPermanent() {
        Permanent huntmaster = addCreatureReady(player1, new CompleatedHuntmaster());
        harness.addToBattlefield(player2, new CompleatedHuntmaster());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(huntmaster), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
