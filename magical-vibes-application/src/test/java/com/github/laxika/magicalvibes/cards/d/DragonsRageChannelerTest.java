package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsRageChanneler.class, Divination.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class})
class DragonsRageChannelerTest extends BaseCardTest {

    @Test
    @DisplayName("Delirium gives Dragon's Rage Channeler +2/+2 and flying")
    void deliriumBoostsChanneler() {
        Permanent channeler = addChanneler(List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));

        assertThat(gqs.getEffectivePower(gd, channeler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, channeler)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, channeler, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Delirium makes Dragon's Rage Channeler attack if able")
    void deliriumRequiresAttack() {
        addChanneler(List.of(new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Casting a noncreature spell triggers surveil 1")
    void noncreatureSpellTriggersSurveil() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        addChanneler(List.of());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger surveil")
    void creatureSpellDoesNotTriggerSurveil() {
        addChanneler(List.of());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addChanneler(List<Card> graveyard) {
        harness.setGraveyard(player1, graveyard);
        return addCreatureReady(player1, new DragonsRageChanneler());
    }
}
