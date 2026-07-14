package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WandOfDenialTest extends BaseCardTest {

    // ===== Nonland top card: pay 2 life to bin it =====

    @Test
    @DisplayName("Paying 2 life puts a nonland top card into the target player's graveyard")
    void paysLifeToBinNonlandCard() {
        addReadyWand(player1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(0, topCard);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Declining leaves the nonland card on top and pays no life")
    void decliningLeavesCardAndPaysNoLife() {
        addReadyWand(player1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(0, topCard);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Land top card: no choice offered =====

    @Test
    @DisplayName("A land top card is left on top with no life payment offered")
    void landTopCardIsUntouched() {
        addReadyWand(player1);

        Card topCard = new Forest();
        gd.playerDecks.get(player2.getId()).add(0, topCard);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Cannot afford the life =====

    @Test
    @DisplayName("No choice offered when the controller cannot pay 2 life")
    void noChoiceWhenCannotPayLife() {
        addReadyWand(player1);
        gd.playerLifeTotals.put(player1.getId(), 1);

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(0, topCard);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolves cleanly when the target library is empty")
    void emptyLibraryResolvesCleanly() {
        addReadyWand(player1);
        gd.playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    // ===== Helpers =====

    private Permanent addReadyWand(Player player) {
        WandOfDenial card = new WandOfDenial();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
