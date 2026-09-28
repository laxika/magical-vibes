package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.p.PatronOfTheKitsune;
import com.github.laxika.magicalvibes.cards.s.SickeningShoal;
import com.github.laxika.magicalvibes.cards.t.TorrentOfStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        KamiOfTheHonoredDead.class,
        GnarledMass.class,
        KamiOfFalseHope.class,
        PatronOfTheKitsune.class,
        SickeningShoal.class,
        TorrentOfStone.class
})
class KamiOfTheHonoredDeadTest extends BaseCardTest {

    /** Gives the Kami -5/-5 so it dies, firing its Soulshift trigger. */
    private void sickeningShoalToKillKami() {
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        UUID kamiId = harness.getPermanentId(player1, "Kami of the Honored Dead");
        harness.castInstant(player1, 0, 5, kamiId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The Kami has flying")
    void hasFlying() {
        var kami = addCreatureReady(player1, new KamiOfTheHonoredDead());

        assertThat(gqs.hasKeyword(gd, kami, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Damage dealt to the Kami gains its controller that much life")
    void damageGainsThatMuchLife() {
        harness.addToBattlefield(player1, new KamiOfTheHonoredDead());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new TorrentOfStone()));
        harness.addMana(player2, ManaColor.RED, 4);

        UUID kamiId = harness.getPermanentId(player1, "Kami of the Honored Dead");
        harness.castInstant(player2, 0, kamiId);
        harness.passBothPriorities(); // Torrent of Stone resolves — 4 damage
        harness.passBothPriorities(); // the trigger resolves

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(findPermanent(player1, "Kami of the Honored Dead").getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage dealt to the Kami also gains its controller that much life")
    void combatDamageGainsThatMuchLife() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new KamiOfTheHonoredDead());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
        assertThat(findPermanent(player2, "Kami of the Honored Dead").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Soulshift 6 returns a targeted Spirit with mana value 6 or less from your graveyard to your hand")
    void deathReturnsSpiritToHand() {
        harness.addToBattlefield(player1, new KamiOfTheHonoredDead());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        sickeningShoalToKillKami();

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
        Card cheapSpirit = new KamiOfFalseHope();
        Card boundarySpirit = new PatronOfTheKitsune(); // mana value 6
        Card expensiveSpirit = new KamiOfTheHonoredDead(); // mana value 7
        Card nonSpirit = new SickeningShoal();
        Card opponentSpirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(cheapSpirit, boundarySpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        sickeningShoalToKillKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cheapSpirit.getId(), boundarySpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift can be declined even when a legal Spirit target exists")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new KamiOfTheHonoredDead());
        Card spirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(spirit));

        sickeningShoalToKillKami();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isZero();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift presents no choice when your graveyard has no matching Spirit")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new KamiOfTheHonoredDead());
        Card nonSpirit = new SickeningShoal();
        harness.setGraveyard(player1, List.of(nonSpirit));

        sickeningShoalToKillKami();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(nonSpirit.getId()));
    }
}
