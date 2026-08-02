package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxOpal;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReweaveTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed creature is replaced by the first creature card revealed")
    void replacesCreatureWithCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new MoxOpal());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        // The artifact did not share a card type with the sacrificed creature, so it was shuffled back
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mox Opal"));
    }

    @Test
    @DisplayName("Sacrificed artifact is replaced by an artifact, skipping a creature card")
    void replacesArtifactWithArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new MoxOpal());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertOnBattlefield(player1, "Mox Opal");
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("No card sharing a type — the whole library is revealed and shuffled back")
    void noSharedTypeInLibrary() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new Forest());

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can target an opponent's land — they sacrifice it and reveal until a land")
    void targetsOpponentLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Reweave()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        GameData gd = harness.getGameData();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player2.getId()).add(new Forest());

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
