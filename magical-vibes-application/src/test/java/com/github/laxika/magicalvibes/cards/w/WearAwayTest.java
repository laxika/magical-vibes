package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WearAwayTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new WearAway()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        WearAway host = new WearAway();
        WearAway spliced = new WearAway();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(host, spliced));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castWithSplice(player1, 0, targetId, List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spliced);
    }
}
