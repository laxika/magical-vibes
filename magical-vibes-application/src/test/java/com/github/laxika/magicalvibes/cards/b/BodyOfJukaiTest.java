package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.PatronOfTheOrochi;
import com.github.laxika.magicalvibes.cards.s.SickeningShoal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyOfJukai.class, PatronOfTheOrochi.class, SickeningShoal.class})
class BodyOfJukaiTest extends BaseCardTest {

    private void sickeningShoalToKillBody() {
        harness.setHand(player1, List.of(new SickeningShoal()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castInstant(player1, 0, 5, harness.getPermanentId(player1, "Body of Jukai"));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new BodyOfJukai());
        Permanent blocker = addCreatureReady(player2, new PatronOfTheOrochi());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 7,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("Soulshift 8 returns a targeted Spirit with mana value 8 or less from your graveyard to your hand")
    void deathReturnsSpiritToHand() {
        harness.addToBattlefield(player1, new BodyOfJukai());
        Card spirit = new PatronOfTheOrochi();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        sickeningShoalToKillBody();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift 8 may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new BodyOfJukai());
        Card spirit = new PatronOfTheOrochi();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        sickeningShoalToKillBody();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spirit.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Spirits with mana value 9 or greater, non-Spirit cards, and an opponent's Spirits are not legal targets")
    void expensiveOrOpponentSpiritNotTargetable() {
        harness.addToBattlefield(player1, new BodyOfJukai());
        Card cheapSpirit = new PatronOfTheOrochi();
        Card expensiveSpirit = new BodyOfJukai();
        Card nonSpirit = new SickeningShoal();
        Card opponentSpirit = new PatronOfTheOrochi();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cheapSpirit, expensiveSpirit, nonSpirit)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentSpirit)));

        sickeningShoalToKillBody();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("With no Spirit with mana value 8 or less in your graveyard the trigger presents no choice")
    void noLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new BodyOfJukai());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new BodyOfJukai())));

        sickeningShoalToKillBody();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
