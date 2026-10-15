package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.Guile;
import com.github.laxika.magicalvibes.cards.w.WildNacatl;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PunishIgnorance.class, WildNacatl.class, Guile.class})
class PunishIgnoranceTest extends BaseCardTest {

    private void addManaForPunish() {
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Counters target spell, its controller loses 3 life and caster gains 3 life")
    void countersAndDrainsLife() {
        WildNacatl nacatl = new WildNacatl();
        harness.setLife(player1, 20);

        harness.setHand(player2, List.of(new PunishIgnorance()));
        addManaForPunish();
        harness.setLife(player2, 20);

        harness.castFromHand(player1, nacatl, "{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nacatl.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wild Nacatl");
        harness.assertNotOnBattlefield(player1, "Wild Nacatl");
        // Its controller loses 3 life, caster gains 3 life
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Countering your own spell loses and gains life for the same player")
    void canCounterOwnSpell() {
        WildNacatl nacatl = new WildNacatl();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, nacatl, "{G}");
        harness.setHand(player1, List.of(new PunishIgnorance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, nacatl.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wild Nacatl");
        harness.assertNotOnBattlefield(player1, "Wild Nacatl");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Neither player changes life when the only target leaves the stack")
    void illegalTargetPreventsLifeChanges() {
        WildNacatl nacatl = new WildNacatl();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, nacatl, "{G}");
        harness.setHand(player2, List.of(new PunishIgnorance()));
        addManaForPunish();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nacatl.getId());

        harness.setHand(player1, List.of(new PunishIgnorance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, nacatl.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Wild Nacatl");
        harness.assertInGraveyard(player2, "Punish Ignorance");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({PunishIgnorance.class, WildNacatl.class, Guile.class})
    @DisplayName("Guile's counter replacement is handled before the spell controller loses life")
    void counterReplacementPrecedesLifeLoss() {
        harness.addToBattlefield(player2, new Guile());
        WildNacatl nacatl = new WildNacatl();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, nacatl, "{G}");
        harness.setHand(player2, List.of(new PunishIgnorance()));
        addManaForPunish();
        harness.passPriority(player1);
        harness.castInstant(player2, 0, nacatl.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(nacatl.getId()));
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 23);
        harness.assertNotInGraveyard(player1, "Wild Nacatl");
    }
}
