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
}
