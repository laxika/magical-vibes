package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castSorcery(player1, 0, List.of(fountainId, ornithopterId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Fountain of Youth"))
                .noneMatch(c -> c.getName().equals("Ornithopter"));
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
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(fountainId, creatureId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
