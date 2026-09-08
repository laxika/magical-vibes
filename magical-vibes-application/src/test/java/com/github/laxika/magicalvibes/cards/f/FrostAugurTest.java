package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FrostAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a snow top card for optional reveal and puts it into hand when accepted")
    void acceptsTopSnowCard() {
        addReadyAugur();
        Card topCard = new SnowCoveredIsland();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));
        addSnowMana();

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("A non-snow top card stays on top without offering a choice")
    void nonSnowTopCardStaysOnTop() {
        addReadyAugur();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, new SnowCoveredIsland()));
        addSnowMana();

        activateAbility();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Snow mana is required to activate Frost Augur")
    void requiresSnowMana() {
        addReadyAugur();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    private void addReadyAugur() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent augur = harness.addToBattlefieldAndReturn(player1, new FrostAugur());
        augur.setSummoningSick(false);
    }

    private void addSnowMana() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLUE, 1);
        pool.addSnowMana(ManaColor.BLUE, 1);
    }

    private void activateAbility() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
