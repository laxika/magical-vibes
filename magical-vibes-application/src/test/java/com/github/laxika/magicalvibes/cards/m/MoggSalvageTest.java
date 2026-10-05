package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoggSalvage.class, FountainOfYouth.class, Forest.class, Island.class, Mountain.class})
class MoggSalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an artifact when cast for its mana cost")
    void destroysArtifactWithManaCost() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Mogg Salvage");
    }

    @Test
    @DisplayName("Destroys an artifact when cast for free with an opponent's Island and your Mountain")
    void destroysArtifactWithAlternateCost() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        harness.castWithAlternateCost(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Mogg Salvage");
    }

    @Test
    @DisplayName("Alternate cast requires an opponent's Island and your Mountain")
    void alternateCastRequiresBothLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cast requires an opponent's Island")
    void alternateCastRequiresOpponentsIsland() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MoggSalvage()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy your own artifact with the alternate cost")
    void destroysOwnArtifact() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        harness.castWithAlternateCost(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Mogg Salvage");
    }

    @Test
    @DisplayName("An opponent's Mountain does not enable the alternate cost")
    void opponentsMountainDoesNotQualify() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May pay the mana cost even when the alternate cost is available")
    void mayChooseNormalManaCost() {
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Tapped qualifying lands enable free casting without being consumed")
    void tappedLandsQualify() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        mountain.setTapped(true);
        island.setTapped(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        harness.castWithAlternateCost(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mountain);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(island);
        assertThat(mountain.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Losing the qualifying lands after casting does not prevent destruction")
    void landConditionIsNotRecheckedOnResolution() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoggSalvage()));

        harness.castWithAlternateCost(player1, 0, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mountain);
        gd.playerGraveyards.get(player1.getId()).add(mountain.getCard());
        gd.playerBattlefields.get(player2.getId()).remove(island);
        gd.playerGraveyards.get(player2.getId()).add(island.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Mogg Salvage");
    }
}
