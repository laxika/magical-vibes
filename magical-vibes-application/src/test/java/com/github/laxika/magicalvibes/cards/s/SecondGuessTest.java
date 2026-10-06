package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GraveExchange;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.p.PillarOfFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecondGuess.class, GrizzlyBears.class, LightningBolt.class,
        GraveExchange.class, MoorlandInquisitor.class, PillarOfFlame.class})
class SecondGuessTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the second spell cast this turn")
    void countersSecondSpell() {
        GrizzlyBears second = new GrizzlyBears();
        harness.setHand(player1, List.of(new LightningBolt(), second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castCreature(player1, 0);

        harness.castAndResolveInstant(player2, 0, second.getId());

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Cannot target the third spell cast this turn")
    void cannotTargetThirdSpell() {
        LightningBolt third = new LightningBolt();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), third));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, third.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target the only spell cast this turn")
    void cannotTargetFirstSpellWhenAlone() {
        GrizzlyBears only = new GrizzlyBears();
        harness.setHand(player1, List.of(only));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, only.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsSpellsCastByBothPlayers() {
        LightningBolt second = new LightningBolt();
        harness.setHand(player1, List.of(new LightningBolt(), new SecondGuess()));
        harness.setHand(player2, List.of(second));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.assertInGraveyard(player2, "Lightning Bolt");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondSpellRemainsTargetableAfterThirdSpellIsCast() {
        LightningBolt second = new LightningBolt();
        harness.setHand(player1, List.of(new LightningBolt(), second, new LightningBolt()));
        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player2, 0, second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetFirstSpellAfterSecondSpellIsCast() {
        LightningBolt first = new LightningBolt();
        harness.setHand(player1, List.of(first, new LightningBolt()));
        harness.setHand(player2, List.of(new SecondGuess()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, first.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void recastCardIsNotStillTheSecondSpell() {
        MoorlandInquisitor creature = new MoorlandInquisitor();
        harness.setHand(player1, List.of(new PillarOfFlame(), creature, new GraveExchange()));
        harness.setHand(player2, List.of(new SecondGuess(), new SecondGuess()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertInGraveyard(player1, "Moorland Inquisitor");

        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, creature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Moorland Inquisitor");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
