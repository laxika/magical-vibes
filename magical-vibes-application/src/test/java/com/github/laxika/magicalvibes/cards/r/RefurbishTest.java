package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.a.AnimationModule;
import com.github.laxika.magicalvibes.cards.b.BastionMastodon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Refurbish.class, AngelsFeather.class, GrizzlyBears.class, AnimationModule.class, BastionMastodon.class})
class RefurbishTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target artifact card from your graveyard to the battlefield")
    void returnsArtifactFromGraveyardToBattlefield() {
        Card artifact = new AngelsFeather();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new Refurbish()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Angel's Feather");
        harness.assertNotInGraveyard(player1, "Angel's Feather");
    }

    @Test
    @DisplayName("Cannot target non-artifact card in graveyard")
    void cannotTargetNonArtifactCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Refurbish()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact card in an opponent's graveyard")
    void cannotTargetOpponentsArtifact() {
        Card artifact = new AnimationModule();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new Refurbish()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns only the targeted artifact creature, untapped under your control")
    void returnsOnlyTargetedArtifactCreature() {
        Card artifact = new BastionMastodon();
        Card otherArtifact = new AnimationModule();
        harness.setGraveyard(player1, List.of(artifact, otherArtifact));
        harness.setHand(player1, List.of(new Refurbish()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bastion Mastodon");
        harness.assertNotInGraveyard(player1, "Bastion Mastodon");
        harness.assertInGraveyard(player1, "Animation Module");
        harness.assertNotOnBattlefield(player1, "Animation Module");
        harness.assertNotOnBattlefield(player2, "Bastion Mastodon");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(artifact.getId()))
                .allSatisfy(p -> assertThat(p.isTapped()).isFalse());
    }

    @Test
    @DisplayName("Does not return another artifact when the target leaves the graveyard")
    void targetLeavingGraveyardDoesNotRetarget() {
        Card artifact = new BastionMastodon();
        Card otherArtifact = new AnimationModule();
        harness.setGraveyard(player1, List.of(artifact, otherArtifact));
        harness.setHand(player1, List.of(new Refurbish()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.setGraveyard(player1, List.of(otherArtifact));
        harness.setExile(player1, List.of(artifact));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bastion Mastodon");
        harness.assertNotOnBattlefield(player1, "Animation Module");
        harness.assertInGraveyard(player1, "Animation Module");
        harness.assertInGraveyard(player1, "Refurbish");
        assertThat(harness.getGameData().findExiledCard(artifact.getId())).isNotNull();
    }
}
