package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AinokGuide;
import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SultaiEmissary.class, GrizzlyBears.class, Shock.class, AinokGuide.class, DouseInGloom.class})
class SultaiEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("When Sultai Emissary dies, its controller manifests the top card of their library")
    void diesManifestsTopCard() {
        Permanent emissary = addCreatureReady(player1, new SultaiEmissary());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, emissary.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested() && permanent.isFaceDown());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Sultai Emissary dies with an empty library, nothing is manifested")
    void diesWithEmptyLibraryDoesNothing() {
        Permanent emissary = addCreatureReady(player1, new SultaiEmissary());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, emissary.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    @DisplayName("Manifested creature turns face up for its mana cost without its enter ability triggering")
    void manifestedCreatureTurnsFaceUpWithoutEnterTrigger() {
        Permanent emissary = addCreatureReady(player1, new SultaiEmissary());
        AinokGuide top = new AinokGuide();
        DouseInGloom next = new DouseInGloom();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, emissary.getId());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isSameAs(top);
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.turnFaceUp(player1, 0);

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(1);
    }

    @Test
    @DisplayName("A noncreature card can be manifested but cannot turn face up for its mana cost")
    void manifestedInstantCannotTurnFaceUp() {
        Permanent emissary = addCreatureReady(player1, new SultaiEmissary());
        DouseInGloom top = new DouseInGloom();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, emissary.getId());
        resolveAllTriggers();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.getCard()).isSameAs(top);
        assertThat(manifested.isManifested()).isTrue();
        assertThat(gqs.getEffectivePower(gd, manifested)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, manifested)).isEqualTo(2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("An opponent's dying Emissary manifests from that opponent's library")
    void opponentManifestsFromOwnLibrary() {
        Permanent emissary = addCreatureReady(player2, new SultaiEmissary());
        AinokGuide ownTop = new AinokGuide();
        DouseInGloom opponentTop = new DouseInGloom();
        harness.setLibrary(player1, List.of(ownTop));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, emissary.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownTop);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(opponentTop);
            assertThat(permanent.isManifested()).isTrue();
            assertThat(permanent.isFaceDown()).isTrue();
        });
        harness.assertInGraveyard(player2, "Sultai Emissary");
    }
}
