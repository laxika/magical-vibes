package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FinalDeath.class, AltarOfThePantheon.class, NyxbornColossus.class})
class FinalDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target creature")
    void exilesCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new NyxbornColossus()).getId();
        castFinalDeath(targetId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nyxborn Colossus");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Nyxborn Colossus"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new AltarOfThePantheon()).getId();

        assertThatThrownBy(() -> castFinalDeath(targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile a creature you control without putting it in the graveyard")
    void exilesOwnCreature() {
        NyxbornColossus creature = new NyxbornColossus();
        UUID targetId = harness.addToBattlefieldAndReturn(player1, creature).getId();
        castFinalDeath(targetId);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nyxborn Colossus");
        harness.assertNotInGraveyard(player1, "Nyxborn Colossus");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Final Death");
    }

    @Test
    @DisplayName("Does nothing when its target is exiled in response")
    void targetLeavesBeforeResolution() {
        NyxbornColossus creature = new NyxbornColossus();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, creature).getId();
        castFinalDeath(targetId);
        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        harness.assertNotInGraveyard(player2, "Nyxborn Colossus");
        harness.assertInGraveyard(player1, "Final Death");
        harness.assertInGraveyard(player2, "Final Death");
        assertThat(gd.stack).isEmpty();
    }

    private void castFinalDeath(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FinalDeath()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, targetId);
    }
}
