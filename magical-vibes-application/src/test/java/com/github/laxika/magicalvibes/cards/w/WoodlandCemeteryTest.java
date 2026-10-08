package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoodlandCemetery.class, Forest.class, Mountain.class, Swamp.class})
class WoodlandCemeteryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent cemetery = findCemetery(player1);
        assertThat(cemetery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Mountain)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent cemetery = findCemetery(player1);
        assertThat(cemetery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent cemetery = findCemetery(player1);
        assertThat(cemetery.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Forest")
    void entersUntappedWithForest() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent cemetery = findCemetery(player1);
        assertThat(cemetery.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Swamp and a Forest")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent cemetery = findCemetery(player1);
        assertThat(cemetery.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Swamp does not satisfy the check")
    void opponentSwampDoesNotCount() {
        harness.addToBattlefield(player2, new Swamp());

        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent cemetery = findCemetery(player1);
        assertThat(cemetery.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new WoodlandCemetery());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        harness.addToBattlefield(player1, new WoodlandCemetery());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Swamp still lets Woodland Cemetery enter untapped")
    void entersUntappedWithTappedSwamp() {
        harness.addToBattlefieldAndReturn(player1, new Swamp()).tap();
        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCemetery(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Swamp and Forest in hand do not satisfy the check")
    void qualifyingLandsInHandDoNotCount() {
        harness.setHand(player1, List.of(new WoodlandCemetery(), new Swamp(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCemetery(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Woodland Cemetery is neither a Swamp nor a Forest")
    void anotherCemeteryDoesNotCount() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new WoodlandCemetery());
        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> !permanent.getId().equals(existing.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isTrue());
    }

    @Test
    @DisplayName("Opponent's Forest does not satisfy the check")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new WoodlandCemetery()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCemetery(player1).isTapped()).isTrue();
    }

    private Permanent findCemetery(Player player) {
        return findPermanent(player, "Woodland Cemetery");
    }
}
