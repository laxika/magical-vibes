package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BenalishEmissary;
import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Winnow.class, BenalishLancer.class, BenalishEmissary.class, Island.class})
class WinnowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys only the target when another same-named permanent exists, then draws")
    void destroysOnlyTargetWhenAnotherSameNamedPermanentExistsAndDraws() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.addToBattlefield(player2, new BenalishEmissary());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        UUID targetId = harness.getPermanentId(player2, "Benalish Lancer");
        prepareWinnow();
        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(countPermanents(player2, "Benalish Lancer")).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Benalish Lancer");
        harness.assertOnBattlefield(player2, "Benalish Emissary");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Benalish Emissary");
    }

    @Test
    @DisplayName("Draws a card without destroying a lone target")
    void drawsWithoutAnotherPermanentWithSameName() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        UUID targetId = harness.getPermanentId(player2, "Benalish Lancer");
        prepareWinnow();
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Benalish Lancer");
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .extracting(card -> card.getName()).isEqualTo("Benalish Emissary");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> castWinnow(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Another controller's same-named permanent satisfies the condition")
    void destroysTargetWhenOnlyOtherControllerHasMatchingPermanent() {
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        prepareWinnow();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Benalish Lancer"));

        harness.assertNotOnBattlefield(player1, "Benalish Lancer");
        harness.assertInGraveyard(player1, "Benalish Lancer");
        harness.assertOnBattlefield(player2, "Benalish Lancer");
        harness.assertInHand(player1, "Benalish Emissary");
    }

    @Test
    @DisplayName("Another copy leaving before resolution prevents destruction but not the draw")
    void checksOtherPermanentAtResolution() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        castWinnow(harness.getPermanentId(player2, "Benalish Lancer"));
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Benalish Lancer"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Benalish Lancer");
        harness.assertInHand(player1, "Benalish Emissary");
    }

    @Test
    @DisplayName("Another copy entering before resolution enables destruction")
    void destroysWhenMatchingPermanentAppearsBeforeResolution() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        castWinnow(harness.getPermanentId(player2, "Benalish Lancer"));
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Benalish Lancer");
        harness.assertInGraveyard(player2, "Benalish Lancer");
        harness.assertOnBattlefield(player1, "Benalish Lancer");
        harness.assertInHand(player1, "Benalish Emissary");
    }

    @Test
    @DisplayName("Does not draw when the sole target leaves before resolution")
    void doesNotDrawWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new BenalishLancer());
        harness.addToBattlefield(player1, new BenalishLancer());
        harness.setLibrary(player1, List.of(new BenalishEmissary()));

        castWinnow(harness.getPermanentId(player2, "Benalish Lancer"));
        gd.playerBattlefields.get(player2.getId()).remove(findPermanent(player2, "Benalish Lancer"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Winnow");
        harness.assertOnBattlefield(player1, "Benalish Lancer");
    }

    private void castWinnow(UUID targetId) {
        prepareWinnow();
        harness.castInstant(player1, 0, targetId);
    }

    private void prepareWinnow() {
        harness.setHand(player1, List.of(new Winnow()));
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
