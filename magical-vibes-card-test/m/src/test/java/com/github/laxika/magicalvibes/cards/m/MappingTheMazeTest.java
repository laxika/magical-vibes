package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LightningHelix;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MappingTheMaze.class, Opt.class, LightningHelix.class})
class MappingTheMazeTest extends BaseCardTest {

    @Test
    void incorporatesCardFromHandAndReturnsMulticoloredSpell() {
        MappingTheMaze mapping = new MappingTheMaze();
        Opt chosen = new Opt();
        LightningHelix returned = new LightningHelix();
        harness.setHand(player1, List.of(mapping, chosen));
        harness.setGraveyard(player1, List.of(returned));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PerpetualHandOrGraveyardCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualHandOrGraveyardCardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(chosen.getId(), returned.getId());
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.perpetualManaCostIncreases).containsKey(chosen.getId());
        assertThat(gd.perpetualTriggeredAbilityGrants.get(chosen.getId()))
                .containsKey(com.github.laxika.magicalvibes.model.EffectSlot.ON_SELF_CAST);
        harness.assertInHand(player1, "Lightning Helix");
        harness.assertNotInGraveyard(player1, "Lightning Helix");
    }

    @Test
    void incorporatesCardFromGraveyard() {
        MappingTheMaze mapping = new MappingTheMaze();
        Opt chosen = new Opt();
        harness.setHand(player1, List.of(mapping));
        harness.setGraveyard(player1, List.of(chosen));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.perpetualManaCostIncreases).containsKey(chosen.getId());
        harness.assertInGraveyard(player1, "Opt");
    }
}
