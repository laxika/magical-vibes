package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexingArcanix.class, Incinerate.class})
class VexingArcanixTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the target player to name a card")
    void resolvingPromptsTargetPlayer() {
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player2.getId());
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.TargetPlayerNameCardRevealTopChoice.class);
    }

    @Test
    @DisplayName("Correct name puts the top card into the target's hand with no damage")
    void correctNameGoesToHand() {
        harness.setLife(player2, 20);
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card topCard = new Incinerate();
        harness.setLibrary(player2, List.of(topCard));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Incinerate");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Wrong name puts the top card into the graveyard and deals 2 damage to the target")
    void wrongNameGoesToGraveyardAndDeals2Damage() {
        harness.setLife(player2, 20);
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card topCard = new Incinerate();
        harness.setLibrary(player2, List.of(topCard));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Vexing Arcanix");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Empty library does not crash and deals no damage")
    void emptyLibraryHandledGracefully() {
        harness.setLife(player2, 20);
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setLibrary(player2, List.of());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Vexing Arcanix");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A miss still deals damage if the source leaves before the name is chosen")
    void missDealsDamageAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        Permanent arcanix = addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card topCard = new Incinerate();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(arcanix);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Vexing Arcanix");

        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addReadyArcanix(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new VexingArcanix());
        perm.setSummoningSick(false);
        return perm;
    }
}
