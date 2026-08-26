package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathcapMarionette.class, Forest.class})
class DeathcapMarionetteTest extends BaseCardTest {

    @Test
    void acceptingEnterTheBattlefieldTriggerMillsTwoCards() {
        castDeathcapMarionette();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void decliningEnterTheBattlefieldTriggerDoesNotMill() {
        castDeathcapMarionette();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    private void castDeathcapMarionette() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new DeathcapMarionette()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
