package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlameJab;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CreakwoodGhoul.class, FlameJab.class, NettleSentinel.class})
class CreakwoodGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target card from a graveyard and controller gains 1 life")
    void exilesCardAndGainsLife() {
        Permanent ghoul = addCreatureReady(player1, new CreakwoodGhoul());
        Card target = new NettleSentinel();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 2);
        int startLife = gd.playerLifeTotals.get(player1.getId());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Nettle Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Nettle Sentinel"));
        harness.assertLife(player1, startLife + 1);
    }

    @Test
    @DisplayName("Can exile a noncreature card from a graveyard")
    void exilesNoncreatureCard() {
        Permanent ghoul = addCreatureReady(player1, new CreakwoodGhoul());
        Card target = new FlameJab();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Flame Jab");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Flame Jab"));
    }

    @Test
    @DisplayName("Can exile a card from controller's own graveyard")
    void exilesFromOwnGraveyard() {
        Permanent ghoul = addCreatureReady(player1, new CreakwoodGhoul());
        Card target = new NettleSentinel();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nettle Sentinel"));
    }

    @Test
    @DisplayName("Accepts one black mana and one green mana for the hybrid cost")
    void acceptsMixedHybridMana() {
        Permanent ghoul = addCreatureReady(player1, new CreakwoodGhoul());
        Card target = new NettleSentinel();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);
        harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Nettle Sentinel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Nettle Sentinel"));
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent ghoul = addCreatureReady(player1, new CreakwoodGhoul());
        Card target = new NettleSentinel();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent ghoul = addCreatureReady(player1, new CreakwoodGhoul());
        Card target = new NettleSentinel();
        harness.addMana(player1, ManaColor.BLACK, 2);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ghoul);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
