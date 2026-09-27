package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AncientDen;
import com.github.laxika.magicalvibes.cards.f.FangrenHunter;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletonShard.class, Ornithopter.class, FangrenHunter.class, AncientDen.class})
class SkeletonShardTest extends BaseCardTest {

    @Test
    @DisplayName("The {3} activation returns an artifact creature from the graveyard to hand")
    void genericActivationReturnsArtifactCreature() {
        Ornithopter ornithopter = new Ornithopter();
        Permanent shard = addShardAndTarget(ornithopter);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(shard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The {B} activation returns an artifact creature from the graveyard to hand")
    void blackActivationReturnsArtifactCreature() {
        Ornithopter ornithopter = new Ornithopter();
        Permanent shard = addShardAndTarget(ornithopter);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
        harness.assertNotInGraveyard(player1, "Ornithopter");
        assertThat(shard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activations cannot target a card that is not an artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new SkeletonShard());
        Card nonArtifact = new FangrenHunter();
        harness.setGraveyard(player1, List.of(nonArtifact));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(nonArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activations cannot target an artifact card that is not a creature")
    void cannotTargetNoncreatureArtifact() {
        harness.addToBattlefield(player1, new SkeletonShard());
        Card nonCreature = new AncientDen();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activations can target only an artifact creature in your graveyard")
    void cannotTargetOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SkeletonShard());
        Card opponentTarget = new Ornithopter();
        harness.setGraveyard(player2, List.of(opponentTarget));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(opponentTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The activations require an untapped Skeleton Shard")
    void activationRequiresUntappedShard() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new SkeletonShard());
        shard.tap();
        Card target = new Ornithopter();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() ->
                harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addShardAndTarget(Card target) {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new SkeletonShard());
        harness.setGraveyard(player1, List.of(target));
        return shard;
    }
}
