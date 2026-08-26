package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HazeFrog;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CroakingCounterpart.class, HazeFrog.class, HillGiant.class})
class CroakingCounterpartTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a green 1/1 Frog token copy with only the Frog creature type")
    void createsGreenOneOneFrogTokenCopy() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();

        UUID targetId = harness.getPermanentId(player1, "Hill Giant");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FROG);
    }

    @Test
    @DisplayName("Cannot target a Frog creature")
    void cannotTargetFrogCreature() {
        harness.addToBattlefield(player1, new HazeFrog());
        harness.setHand(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartMana();

        UUID targetId = harness.getPermanentId(player1, "Haze Frog");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback creates the Frog token and exiles the spell")
    void flashbackCreatesTokenAndExilesSpell() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setGraveyard(player1, List.of(new CroakingCounterpart()));
        addCroakingCounterpartFlashbackMana();

        UUID targetId = harness.getPermanentId(player1, "Hill Giant");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Croaking Counterpart"));
    }

    private void addCroakingCounterpartMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addCroakingCounterpartFlashbackMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
