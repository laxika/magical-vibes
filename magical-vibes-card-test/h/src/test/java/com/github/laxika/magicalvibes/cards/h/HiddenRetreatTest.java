package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FlameWave;
import com.github.laxika.magicalvibes.cards.f.FoulImp;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HiddenRetreat.class, Shock.class, FlameWave.class, FoulImp.class})
class HiddenRetreatTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents damage from the targeted spell and puts the activation cost on top of the library")
    void preventsTargetedSpellDamage() {
        harness.addToBattlefield(player1, new HiddenRetreat());
        Card chosenCard = new Shock();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(chosenCard));
        harness.setHand(player2, List.of(shock));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(chosenCard);
    }

    @Test
    @DisplayName("Prevents all damage from the targeted sorcery")
    void preventsTargetedSorceryDamage() {
        harness.addToBattlefield(player1, new HiddenRetreat());
        var creature = harness.addToBattlefieldAndReturn(player1, new FoulImp());
        Card chosenCard = new Shock();
        FlameWave flameWave = new FlameWave();
        harness.setHand(player1, List.of(chosenCard));
        harness.setHand(player2, List.of(flameWave));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 7);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, flameWave.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void rejectsCreatureSpellTarget() {
        harness.addToBattlefield(player1, new HiddenRetreat());
        Card chosenCard = new Shock();
        FoulImp creatureSpell = new FoulImp();
        harness.setHand(player1, List.of(chosenCard));
        harness.setHand(player2, List.of(creatureSpell));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureSpell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("instant or sorcery");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosenCard);
    }

    @Test
    @DisplayName("The hand card is paid before resolution, even with an empty library")
    void paysCostBeforeResolutionWithEmptyLibrary() {
        harness.addToBattlefield(player1, new HiddenRetreat());
        Shock chosenCard = new Shock();
        Shock spell = new Shock();
        harness.setHand(player1, List.of(chosenCard));
        harness.setHand(player2, List.of(spell));
        harness.setLibrary(player1, List.of());
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosenCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Cannot activate without a card in hand to pay the cost")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new HiddenRetreat());
        Shock spell = new Shock();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Can prevent its controller's spell without preventing a different spell")
    void preventsOwnSpellButNotAnotherSpell() {
        harness.addToBattlefield(player1, new HiddenRetreat());
        Shock spell = new Shock();
        harness.setHand(player1, List.of(spell, new FoulImp()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shock");
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
