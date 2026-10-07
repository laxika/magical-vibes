package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SubjugatorAngel.class, GrizzlyBears.class, LlanowarElves.class})
class SubjugatorAngelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps all creatures opponents control")
    void etbTapsOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new SubjugatorAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears")
                        || p.getCard().getName().equals("Llanowar Elves"))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("ETB does not tap creatures you control")
    void etbDoesNotTapOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        harness.setHand(player1, List.of(new SubjugatorAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent elves = findPermanent(player2, "Llanowar Elves");
        Permanent angel = findPermanent(player1, "Subjugator Angel");

        assertThat(bears.isTapped()).isFalse();
        assertThat(angel.isTapped()).isFalse();
        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @CardUsed({Forest.class})
    @DisplayName("ETB leaves noncreatures untapped and already tapped creatures tapped")
    void etbOnlyTapsCreatures() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player2, new SubjugatorAngel());
        tappedCreature.tap();
        Permanent untappedCreature = harness.addToBattlefieldAndReturn(player2, new SubjugatorAngel());
        harness.setHand(player1, List.of(new SubjugatorAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(land.isTapped()).isFalse();
        assertThat(tappedCreature.isTapped()).isTrue();
        assertThat(untappedCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB taps creatures entering between the trigger and its resolution")
    void etbUsesBattlefieldAtResolution() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new SubjugatorAngel());
        harness.setHand(player1, List.of(new SubjugatorAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(original.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
        Permanent newcomer = harness.addToBattlefieldAndReturn(player2, new SubjugatorAngel());
        resolveAllTriggers();

        assertThat(original.isTapped()).isTrue();
        assertThat(newcomer.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Subjugator Angel").isTapped()).isFalse();
    }

    @Test
    @DisplayName("ETB resolves when the opponent controls no creatures")
    void etbResolvesWithoutOpponentCreatures() {
        harness.setHand(player1, List.of(new SubjugatorAngel()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanent(player1, "Subjugator Angel").isTapped()).isFalse();
    }
}
