package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OreadOfMountainsBlaze.class, Forest.class})
class OreadOfMountainsBlazeTest extends BaseCardTest {

    @Test
    void discardsACardAndDrawsACard() {
        Permanent oread = harness.addToBattlefieldAndReturn(player1, new OreadOfMountainsBlaze());
        Card discarded = new OreadOfMountainsBlaze();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oread);
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new OreadOfMountainsBlaze());
        harness.setHand(player1, List.of());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void discardIsPaidBeforeDrawResolves() {
        harness.addToBattlefield(player1, new OreadOfMountainsBlaze());
        Forest discarded = new Forest();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void canActivateRepeatedlyWhileTappedAndSummoningSick() {
        Permanent oread = harness.addToBattlefieldAndReturn(player1, new OreadOfMountainsBlaze());
        oread.tap();
        oread.setSummoningSick(true);
        Forest firstDiscard = new Forest();
        OreadOfMountainsBlaze secondDiscard = new OreadOfMountainsBlaze();
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard, secondDiscard);
        assertThat(oread.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutRedMana() {
        harness.addToBattlefield(player1, new OreadOfMountainsBlaze());
        Forest discarded = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
