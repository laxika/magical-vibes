package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WandOfDenial.class, Forest.class, GrizzlyBears.class})
class WandOfDenialTest extends BaseCardTest {

    // ===== Nonland top card: pay 2 life to bin it =====

    @Test
    @DisplayName("Paying 2 life puts a nonland top card into the target player's graveyard")
    void paysLifeToBinNonlandCard() {
        Permanent wand = harness.addToBattlefieldAndReturn(player1, new WandOfDenial());

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(wand.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Declining leaves the nonland card on top and pays no life")
    void decliningLeavesCardAndPaysNoLife() {
        harness.addToBattlefieldAndReturn(player1, new WandOfDenial());

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
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
        harness.addToBattlefieldAndReturn(player1, new WandOfDenial());

        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));
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
        harness.addToBattlefieldAndReturn(player1, new WandOfDenial());
        harness.setLife(player1, 1);

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying exactly 2 life puts the nonland card into the target player's graveyard")
    void canPayExactlyTwoLife() {
        harness.addToBattlefieldAndReturn(player1, new WandOfDenial());
        harness.setLife(player1, 2);

        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.getLife(player1.getId())).isZero();
    }

    // ===== Empty library =====

    @Test
    @DisplayName("Resolves cleanly when the target library is empty")
    void emptyLibraryResolvesCleanly() {
        harness.addToBattlefieldAndReturn(player1, new WandOfDenial());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
