package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({FlashPhotography.class, GrizzlyBears.class, Plains.class})
class FlashPhotographyTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of any permanent you control")
    void createsTokenCopyOfPermanentYouControl() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new FlashPhotography()));
        addNormalMana();

        UUID targetId = harness.getPermanentId(player1, "Plains");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Plains"))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlashPhotography()));
        addNormalMana();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast as though it had flash when targeting your permanent")
    void canBeCastAtInstantSpeedForYourPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlashPhotography()));
        addNormalMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Flashback creates the copy and exiles Flash Photography")
    void flashbackCreatesCopyAndExilesSpell() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new FlashPhotography()));
        addFlashbackMana();

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Flash Photography"));
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
