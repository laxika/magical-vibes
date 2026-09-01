package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
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

@CardUsed({DustToDust.class, FountainOfYouth.class, TormodsCrypt.class, BogImp.class})
class DustToDustTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two target artifacts (not to graveyard)")
    void exilesTwoArtifacts() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID cryptId = harness.getPermanentId(player2, "Tormod's Crypt");
        harness.castSorcery(player1, 0, List.of(fountainId, cryptId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Tormod's Crypt");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fountain of Youth"))
                .anyMatch(c -> c.getName().equals("Tormod's Crypt"));
    }

    @Test
    @DisplayName("Still exiles the remaining artifact when one target is removed before resolution")
    void exilesRemainingWhenOneTargetRemoved() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.setHand(player1, List.of(new DustToDust()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");
        UUID cryptId = harness.getPermanentId(player2, "Tormod's Crypt");
        harness.castSorcery(player1, 0, List.of(fountainId, cryptId));

        // Remove only one target before resolution
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Fountain of Youth"));

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Tormod's Crypt"));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
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
}
