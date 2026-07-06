package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatherTheTownsfolkTest extends BaseCardTest {

    private long humanTokenCount(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Human"))
                .count();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving at high life creates two 1/1 white Human tokens")
    void resolvingCreatesTwoTokens() {
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Human"))
                .toList();
        assertThat(tokens).hasSize(2);

        for (Permanent token : tokens) {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.HUMAN);
        }
    }

    @Test
    @DisplayName("Fateful hour creates five tokens when at 5 life")
    void fatefulHourCreatesFiveAtFiveLife() {
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Fateful hour creates five tokens when below 5 life")
    void fatefulHourCreatesFiveBelowFiveLife() {
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player1.getId())).isEqualTo(5);
    }

    @Test
    @DisplayName("Only two tokens are created at 6 life (above threshold)")
    void createsTwoAboveThreshold() {
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Tokens enter under the controller's control")
    void tokensEnterUnderControllerControl() {
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(humanTokenCount(player1.getId())).isEqualTo(2);
        assertThat(humanTokenCount(player2.getId())).isZero();
    }

    @Test
    @DisplayName("Gather the Townsfolk goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new GatherTheTownsfolk()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gather the Townsfolk"));
    }
}
