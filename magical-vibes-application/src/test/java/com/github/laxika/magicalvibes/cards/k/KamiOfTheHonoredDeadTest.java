package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KamiOfTheHonoredDeadTest extends BaseCardTest {

    /** Wraths the board so the Kami dies, firing its soulshift trigger. */
    private void wrathToKillKami() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Damage dealt to the Kami gains its controller that much life")
    void damageGainsThatMuchLife() {
        harness.addToBattlefield(player1, new KamiOfTheHonoredDead());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID kamiId = harness.getPermanentId(player1, "Kami of the Honored Dead");
        harness.castInstant(player2, 0, kamiId);
        harness.passBothPriorities(); // Shock resolves — 2 damage
        harness.passBothPriorities(); // the trigger resolves

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        harness.assertOnBattlefield(player1, "Kami of the Honored Dead"); // 3/5 survives 2 damage
    }

    @Test
    @DisplayName("Soulshift 6 returns a targeted Spirit with mana value 6 or less from your graveyard to your hand")
    void deathReturnsSpiritToHand() {
        harness.addToBattlefield(player1, new KamiOfTheHonoredDead());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        wrathToKillKami();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("A Spirit with mana value 7 and an opponent's Spirit are not legal soulshift targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new KamiOfTheHonoredDead());
        Card cheapSpirit = new LanternKami();
        Card expensiveSpirit = new KamiOfTheHonoredDead(); // mana value 7
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cheapSpirit, expensiveSpirit)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentSpirit)));

        wrathToKillKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), opponentSpirit.getId());
    }
}
