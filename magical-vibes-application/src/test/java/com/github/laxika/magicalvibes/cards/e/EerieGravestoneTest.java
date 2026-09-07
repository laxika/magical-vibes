package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EerieGravestone.class, Forest.class, GrizzlyBears.class, Shock.class})
class EerieGravestoneTest extends BaseCardTest {

    @Test
    void drawsACardWhenItEnters() {
        harness.setHand(player1, List.of(new EerieGravestone()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    void sacrificesMillsAndMayReturnsAMilledCreature() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new EerieGravestone());
        harness.setLibrary(player1, List.of(creature, new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Eerie Gravestone");
        harness.assertInGraveyard(player1, "Eerie Gravestone");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    void doesNotOfferNoncreaturesFromTheMilledCards() {
        harness.addToBattlefield(player1, new EerieGravestone());
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof Shock);
    }
}
