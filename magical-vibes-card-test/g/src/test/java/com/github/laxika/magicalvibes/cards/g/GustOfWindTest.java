package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PhaseDolphin;
import com.github.laxika.magicalvibes.cards.s.SleeperDart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GustOfWind.class, Glimmerbell.class, PhaseDolphin.class, Island.class, SleeperDart.class})
class GustOfWindTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {1}{U} when you control a creature with flying")
    void costsTwoLessWithFlyingCreature() {
        harness.addToBattlefield(player1, new Glimmerbell());
        harness.addToBattlefield(player2, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player2, "Phase Dolphin");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot use the reduced cost without a creature with flying")
    void doesNotGetReductionWithoutFlyingCreature() {
        harness.addToBattlefield(player2, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player2, "Phase Dolphin");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns an opposing nonland permanent and draws a card")
    void returnsOpposingPermanentAndDrawsCard() {
        harness.addToBattlefield(player2, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player2, "Phase Dolphin");
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Phase Dolphin");
        harness.assertInHand(player2, "Phase Dolphin");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Cannot target a permanent you control")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player1, "Phase Dolphin");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    void opponentsFlyingCreatureDoesNotReduceCost() {
        harness.addToBattlefield(player2, new Glimmerbell());
        UUID targetId = harness.getPermanentId(player2, "Glimmerbell");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleFlyingCreaturesDoNotStackReduction() {
        harness.addToBattlefield(player1, new Glimmerbell());
        harness.addToBattlefield(player1, new Glimmerbell());
        harness.addToBattlefield(player2, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player2, "Phase Dolphin");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsNoncreatureArtifactAndDrawsCard() {
        harness.addToBattlefield(player2, new SleeperDart());
        UUID targetId = harness.getPermanentId(player2, "Sleeper Dart");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Sleeper Dart");
        harness.assertInHand(player2, "Sleeper Dart");
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Gust of Wind");
    }

    @Test
    void doesNotDrawWhenOnlyTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player2, "Phase Dolphin");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Gust of Wind");
    }

    @Test
    void losingFlyingCreatureAfterCastingDoesNotPreventResolution() {
        harness.addToBattlefield(player1, new Glimmerbell());
        harness.addToBattlefield(player2, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player2, "Phase Dolphin");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Phase Dolphin");
        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Gust of Wind");
    }

    @Test
    void doesNotDrawWhenTargetComesUnderYourControl() {
        harness.addToBattlefield(player2, new PhaseDolphin());
        UUID targetId = harness.getPermanentId(player2, "Phase Dolphin");
        harness.setHand(player1, List.of(new GustOfWind()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);
        var target = gd.playerBattlefields.get(player2.getId()).removeFirst();
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phase Dolphin");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Gust of Wind");
    }
}
