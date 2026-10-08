package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.e.EncroachingMycosynth;
import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.v.VulshokReplica;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlobadIronGoblin.class, MyrBattlesphere.class, CopperMyr.class, RagingGoblin.class,
        Memnite.class, VulshokReplica.class, SpikeshotElder.class, EncroachingMycosynth.class})
class SlobadIronGoblinTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact adds red mana equal to its mana value")
    void sacrificeAddsManaEqualToManaValue() {
        addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Myr Battlesphere");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(7);
    }

    @Test
    @DisplayName("Artifact-only red mana can cast an artifact spell")
    void artifactOnlyManaCastsArtifactSpell() {
        addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new CopperMyr()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(5);
    }

    @Test
    @DisplayName("Artifact-only red mana cannot cast a nonartifact spell")
    void artifactOnlyManaCannotCastNonartifactSpell() {
        addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RagingGoblin()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(7);
    }

    @Test
    void chosenArtifactDeterminesManaWhenSeveralArtifactsAreAvailable() {
        addCreatureReady(player1, new SlobadIronGoblin());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        harness.addToBattlefield(player1, new MyrBattlesphere());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, myr.getId());

        harness.assertInGraveyard(player1, "Copper Myr");
        harness.assertOnBattlefield(player1, "Myr Battlesphere");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void manaAbilityResolvesImmediatelyAndTapsSlobad() {
        Permanent slobad = addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new CopperMyr());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(slobad.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Copper Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedRedManaPaysArtifactAbilityIncludingColoredCost() {
        addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new VulshokReplica());

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(5);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vulshok Replica");
        harness.assertLife(player2, 17);
    }

    @Test
    void restrictedManaPaysAbilityOfCreatureMadeArtifactByStaticEffect() {
        addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new SpikeshotElder());
        harness.addToBattlefield(player1, new EncroachingMycosynth());

        harness.activateAbility(player1, 1, 0, null, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(4);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void restrictedManaCannotPayNonartifactAbility() {
        addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new MyrBattlesphere());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addToBattlefield(player1, new SpikeshotElder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isEqualTo(7);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void zeroManaValueArtifactIsSacrificedWithoutProducingMana() {
        Permanent slobad = addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new Memnite());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(slobad.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Memnite");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickSlobadCannotActivate() {
        harness.addToBattlefield(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player1, new CopperMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Copper Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isZero();
    }

    @Test
    void opponentsArtifactCannotPaySacrificeCost() {
        Permanent slobad = addCreatureReady(player1, new SlobadIronGoblin());
        harness.addToBattlefield(player2, new CopperMyr());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(slobad.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Copper Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.RED)).isZero();
    }
}
