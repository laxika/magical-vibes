package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EerieGravestone.class, Forest.class, LurkingLizards.class, Shock.class})
class EerieGravestoneTest extends BaseCardTest {

    @Test
    void drawsACardWhenItEnters() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new EerieGravestone(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Forest.class);
    }

    @Test
    void sacrificesMillsAndMayReturnsAMilledCreature() {
        LurkingLizards creature = new LurkingLizards();
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

    @Test
    void canDeclineEveryCreatureAndLeavesOlderGraveyardCardsAlone() {
        LurkingLizards olderCreature = new LurkingLizards();
        LurkingLizards first = new LurkingLizards();
        LurkingLizards second = new LurkingLizards();
        harness.setGraveyard(player1, List.of(olderCreature));
        harness.addToBattlefield(player1, new EerieGravestone());
        harness.setLibrary(player1, List.of(first, second, new Shock(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(6).contains(olderCreature, first, second);
    }

    @Test
    void canChooseTheSecondCreatureButReturnsOnlyOne() {
        LurkingLizards first = new LurkingLizards();
        LurkingLizards second = new LurkingLizards();
        LurkingLizards third = new LurkingLizards();
        harness.addToBattlefield(player1, new EerieGravestone());
        harness.setLibrary(player1, List.of(first, second, third, new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(4).contains(first, third).doesNotContain(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsTheRemainingCardsWhenFewerThanFourRemain() {
        LurkingLizards creature = new LurkingLizards();
        harness.addToBattlefield(player1, new EerieGravestone());
        harness.setLibrary(player1, List.of(creature, new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
