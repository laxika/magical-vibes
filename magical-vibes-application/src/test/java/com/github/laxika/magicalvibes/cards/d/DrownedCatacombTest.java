package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({DrownedCatacomb.class, Forest.class, Island.class, Swamp.class})
class DrownedCatacombTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Forest)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both an Island and a Swamp")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's Island does not satisfy the check")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player2, new Island());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addCatacombReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addCatacombReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Island still allows Drowned Catacomb to enter untapped")
    void tappedIslandStillQualifies() {
        harness.addToBattlefieldAndReturn(player1, new Island()).tap();
        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findCatacomb(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another Drowned Catacomb does not qualify as an Island or Swamp")
    void anotherCatacombDoesNotQualify() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getLast().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Drowned Catacomb enters tapped even when put onto the battlefield without being played")
    void putOntoBattlefieldWithoutQualifyingLand() {
        Permanent catacomb = harness.enterBattlefieldAndReturn(player1, new DrownedCatacomb());

        assertThat(catacomb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Drowned Catacomb enters untapped when put onto the battlefield with a Swamp")
    void putOntoBattlefieldWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        Permanent catacomb = harness.enterBattlefieldAndReturn(player1, new DrownedCatacomb());

        assertThat(catacomb.isTapped()).isFalse();
    }

    private Permanent addCatacombReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DrownedCatacomb());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent findCatacomb(Player player) {
        return findPermanent(player, "Drowned Catacomb");
    }
}
