package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SproutingPhytohydra.class, Shock.class})
class SproutingPhytohydraTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the damage trigger creates a token copy")
    void acceptingDamageTriggerCreatesTokenCopy() {
        UUID phytohydraId = addPhytohydraAndShock();

        harness.castInstant(player1, 0, phytohydraId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the damage trigger creates no token")
    void decliningDamageTriggerCreatesNoToken() {
        UUID phytohydraId = addPhytohydraAndShock();

        harness.castInstant(player1, 0, phytohydraId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(tokenCount()).isZero();
    }

    @Test
    @DisplayName("Token copies retain the damage trigger")
    void tokenCopiesRetainDamageTrigger() {
        UUID phytohydraId = addPhytohydraAndShock();

        harness.castInstant(player1, 0, phytohydraId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        UUID tokenId = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(permanent -> permanent.getId())
                .findFirst()
                .orElseThrow();
        harness.castInstant(player1, 0, tokenId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(tokenCount()).isEqualTo(1);
    }

    private UUID addPhytohydraAndShock() {
        harness.addToBattlefield(player2, new SproutingPhytohydra());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        return harness.getPermanentId(player2, "Sprouting Phytohydra");
    }

    private long tokenCount() {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
