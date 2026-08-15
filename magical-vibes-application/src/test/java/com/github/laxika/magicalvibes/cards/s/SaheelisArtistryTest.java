package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SaheelisArtistryTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of a target artifact")
    void createsTokenCopyOfArtifact() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player2, new MindStone());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(mindStone.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates an artifact token copy of a target creature")
    void createsArtifactTokenCopyOfCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(bears.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).singleElement().satisfies(token -> {
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        });
    }

    @Test
    @DisplayName("Can choose both modes and target the same permanent")
    void choosesBothModesWithSharedTarget() {
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        prepareCard();

        UUID targetId = ornithopter.getId();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(targetId, targetId), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Rejects a target that does not match the chosen mode")
    void rejectsInvalidModeTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(bears.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new SaheelisArtistry()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private List<Permanent> tokenCopies(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
