package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClavileOFirstOfTheBlessed.class, VampireOfTheDireMoon.class, Shock.class, GrizzlyBears.class})
class ClavileOFirstOfTheBlessedTest extends BaseCardTest {

    @Test
    @DisplayName("An attacking Vampire becomes a Demon and creates a Vampire Demon when it dies")
    void attackingVampireGetsDeathTrigger() {
        addCreatureReady(player1, new ClavileOFirstOfTheBlessed());
        Permanent vampire = addCreatureReady(player1, new VampireOfTheDireMoon());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(vampire.getId());
        harness.handlePermanentChosen(player1, vampire.getId());
        resolveAllTriggers();

        assertThat(vampire.getGrantedSubtypes()).contains(CardSubtype.DEMON);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, vampire.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
    }

    @Test
    @DisplayName("A non-Vampire attacker cannot be chosen")
    void nonVampireAttackerIsNotTargetable() {
        addCreatureReady(player1, new ClavileOFirstOfTheBlessed());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bears.getGrantedSubtypes()).doesNotContain(CardSubtype.DEMON);
    }
}
