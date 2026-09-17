package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CordialVampire;
import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PathOfAncestry.class, EdgarMarkov.class, CordialVampire.class, GrizzlyBears.class})
class PathOfAncestryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());

        assertThat(path.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Produces mana from the commander's color identity")
    void producesCommandIdentityMana() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("WHITE", "BLACK", "RED");

        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scries when its mana casts a creature sharing a type with the commander")
    void scriesOnMatchingCreatureSpell() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        Card creature = new CordialVampire();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new com.github.laxika.magicalvibes.service.interaction.InteractionAnswer.ScryOrder(
                        List.of(0), List.of()));
    }

    @Test
    @DisplayName("Does not scry for a creature with no shared type")
    void doesNotScryOnNonmatchingCreatureSpell() {
        gd.playerCommandZones.get(player1.getId()).add(new EdgarMarkov());
        Permanent path = harness.enterBattlefieldAndReturn(player1, new PathOfAncestry());
        path.untap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }
}
