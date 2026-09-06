package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MysticMeltingTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        castMysticMelting(harness.getPermanentId(player2, "Fountain of Youth"));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Destroys a target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        castMysticMelting(harness.getPermanentId(player2, "Angelic Chorus"));

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsAtNextUpkeep() {
        GameData gd = harness.getGameData();
        harness.addToBattlefield(player2, new FountainOfYouth());
        castMysticMelting(harness.getPermanentId(player2, "Fountain of Youth"));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        gd.activePlayerId = player2.getId();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castMysticMelting(
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMysticMelting(UUID targetId) {
        harness.setHand(player1, List.of(new MysticMelting()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
