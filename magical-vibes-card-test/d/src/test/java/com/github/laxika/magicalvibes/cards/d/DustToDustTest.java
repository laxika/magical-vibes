package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DustToDust.class, FountainOfYouth.class, Ornithopter.class, BogImp.class})
class DustToDustTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two target artifacts (not to graveyard)")
    void exilesTwoArtifacts() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castAndResolveSorcery(player1, 0, List.of(fountainId, ornithopterId));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
    }

    @Test
    @DisplayName("Still exiles the remaining artifact when one target is removed before resolution")
    void exilesRemainingWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castSorcery(player1, 0, List.of(fountainId, ornithopterId));

        // Remove only one target before resolution
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Fountain of Youth"));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when both targets are removed before resolution")
    void doesNothingWhenBothTargetsAreRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castSorcery(player1, 0, List.of(fountainId, ornithopterId));

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dust to Dust");
    }

    @Test
    @DisplayName("Cannot choose the same artifact for both targets")
    void cannotTargetSameArtifactTwice() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(fountainId, fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new BogImp());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID creatureId = harness.getPermanentId(player2, "Bog Imp");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(fountainId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Dust to Dust");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void exilesArtifactsControlledByDifferentPlayers() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castAndResolveSorcery(player1, 0, List.of(fountainId, ornithopterId));

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Ornithopter"));
        harness.assertInGraveyard(player1, "Dust to Dust");
    }

    @Test
    void exilesFirstArtifactWhenSecondTargetIsRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID ornithopterId = harness.getPermanentId(player2, "Ornithopter");
        harness.castSorcery(player1, 0, List.of(fountainId, ornithopterId));
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(ornithopterId));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName()).containsExactly("Fountain of Youth");
        harness.assertInGraveyard(player1, "Dust to Dust");
    }
}
