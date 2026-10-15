package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirgeFamiliar.class, DarkRitual.class})
class SkirgeFamiliarTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card adds one black mana")
    void discardAddsBlackMana() {
        harness.addToBattlefield(player1, new SkirgeFamiliar());
        harness.setHand(player1, List.of(new DarkRitual()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dark Ritual");
    }

    @Test
    @DisplayName("Discards the card chosen from a multi-card hand")
    void discardsChosenCardFromMultipleCards() {
        harness.addToBattlefield(player1, new SkirgeFamiliar());
        harness.setHand(player1, List.of(new SkirgeFamiliar(), new DarkRitual()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dark Ritual");
        harness.assertInHand(player1, "Skirge Familiar");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardInHand() {
        harness.addToBattlefield(player1, new SkirgeFamiliar());
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can repeatedly discard while tapped and summoning sick without using the stack")
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        var familiar = harness.addToBattlefieldAndReturn(player1, new SkirgeFamiliar());
        familiar.setSummoningSick(true);
        familiar.tap();
        harness.setHand(player1, List.of(new DarkRitual(), new SkirgeFamiliar()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Dark Ritual");

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Skirge Familiar");
        harness.assertOnBattlefield(player1, "Skirge Familiar");
        assertThat(familiar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability resolves immediately while a spell is on the stack")
    void resolvesImmediatelyWithSpellOnStack() {
        harness.addToBattlefield(player1, new SkirgeFamiliar());
        harness.setHand(player1, List.of(new DarkRitual(), new SkirgeFamiliar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
        var pendingSpell = gd.stack.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).containsExactly(pendingSpell);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Skirge Familiar");
        harness.assertNotInGraveyard(player1, "Dark Ritual");
    }
}
