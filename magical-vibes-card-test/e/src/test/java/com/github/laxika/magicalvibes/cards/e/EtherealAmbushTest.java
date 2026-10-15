package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtherealAmbush.class, GrizzlyBears.class, Forest.class})
class EtherealAmbushTest extends BaseCardTest {

    @Test
    void manifestsTheTopTwoCards() {
        Card firstCard = new GrizzlyBears();
        Card secondCard = new Forest();
        Card remainingCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of(firstCard, secondCard, remainingCard));
        addEtherealAmbushMana();

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .toList();
        assertThat(manifested).hasSize(2);
        assertThat(manifested).allMatch(Permanent::isFaceDown);
        assertThat(manifested)
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstCard.getId(), secondCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    void manifestsOnlyAvailableCard() {
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of(topCard));
        addEtherealAmbushMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested)
                .map(permanent -> permanent.getCard().getId()))
                .containsExactly(topCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNothingWithEmptyLibrary() {
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of());
        addEtherealAmbushMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void manifestedCreatureCanTurnFaceUpWithoutTurningUpTheLand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of(creature, land));
        addEtherealAmbushMana();

        harness.castAndResolveInstant(player1, 0);

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent manifestedCreature = battlefield.getFirst();
        Permanent manifestedLand = battlefield.get(1);
        assertThat(manifestedCreature.getCard().getId()).isEqualTo(creature.getId());
        assertThat(manifestedLand.getCard().getId()).isEqualTo(land.getId());
        assertThat(gqs.getEffectivePower(gd, manifestedLand)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifestedLand)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, 0);

        assertThat(manifestedCreature.isFaceDown()).isFalse();
        assertThat(manifestedCreature.isManifested()).isFalse();
        assertThat(manifestedLand.isFaceDown()).isTrue();
        assertThat(manifestedLand.isManifested()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(battlefield).containsExactly(manifestedCreature, manifestedLand);
    }

    @Test
    void manifestedLandCannotTurnFaceUpByPayingMana() {
        harness.setHand(player1, List.of(new EtherealAmbush()));
        harness.setLibrary(player1, List.of(new Forest()));
        addEtherealAmbushMana();
        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
    }

    private void addEtherealAmbushMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
