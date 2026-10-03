package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MyrQuadropod;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokSalvagers.class, MyrQuadropod.class, ParadiseMantle.class, SparkElemental.class,
        WayfarersBauble.class})
class AuriokSalvagersTest extends BaseCardTest {

    @Test
    void cannotChooseMultipleTargetsForOneActivation() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card firstArtifact = new ParadiseMantle();
        Card secondArtifact = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(firstArtifact, secondArtifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstArtifact, secondArtifact);
    }

    @Test
    void resolvesWhileSourceIsTappedAndSummoningSick() {
        var salvagers = harness.addToBattlefieldAndReturn(player1, new AuriokSalvagers());
        salvagers.tap();
        salvagers.setSummoningSick(true);
        Card artifact = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(salvagers.isTapped()).isTrue();
    }

    @Test
    void doesNotReturnAnotherArtifactWhenTargetLeavesGraveyard() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card target = new WayfarersBauble();
        Card otherArtifact = new ParadiseMantle();
        harness.setGraveyard(player1, List.of(target, otherArtifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherArtifact));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target, otherArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherArtifact);
    }

    @Test
    void returnsTargetArtifactWithManaValueOneOrLess() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card zeroManaArtifact = new ParadiseMantle();
        Card oneManaArtifact = new WayfarersBauble();
        Card expensiveArtifact = new MyrQuadropod();
        Card nonArtifact = new SparkElemental();
        harness.setGraveyard(player1, List.of(zeroManaArtifact, oneManaArtifact, expensiveArtifact, nonArtifact));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(zeroManaArtifact.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(oneManaArtifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(zeroManaArtifact, oneManaArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(expensiveArtifact, nonArtifact);
    }

    @Test
    void cannotTargetArtifactWithManaValueGreaterThanOne() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card artifact = new MyrQuadropod();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNonArtifactWithManaValueOneOrLess() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card nonArtifact = new SparkElemental();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetArtifactInOpponentsGraveyard() {
        harness.addToBattlefield(player1, new AuriokSalvagers());
        Card artifact = new WayfarersBauble();
        harness.setGraveyard(player2, List.of(artifact));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
