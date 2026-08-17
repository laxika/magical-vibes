package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GiantTortoise;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvertInventTest extends BaseCardTest {

    private static final int INVERT = 0;
    private static final int INVENT = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Invert switches the power and toughness of up to two target creatures")
    void invertSwitchesTwoCreatures() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        int tortoisePower = tortoise.getEffectivePower();
        int tortoiseToughness = tortoise.getEffectiveToughness();
        int wallPower = wall.getEffectivePower();
        int wallToughness = wall.getEffectiveToughness();

        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castModalInstant(player1, 0, INVERT, List.of(tortoise.getId(), wall.getId()));
        harness.passBothPriorities();

        assertThat(tortoise.getEffectivePower()).isEqualTo(tortoiseToughness);
        assertThat(tortoise.getEffectiveToughness()).isEqualTo(tortoisePower);
        assertThat(wall.getEffectivePower()).isEqualTo(wallToughness);
        assertThat(wall.getEffectiveToughness()).isEqualTo(wallPower);
    }

    @Test
    @DisplayName("Invert can be cast with no targets")
    void invertCanChooseNoTargets() {
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, INVERT, List.of());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Invert cannot target a player")
    void invertCannotTargetPlayer() {
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, INVERT, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Invent searches for an instant and a sorcery and puts them into hand")
    void inventSearchesForBothCardTypes() {
        harness.setLibrary(player1, List.of(new Shock(), new Divination(), new WallOfAir()));
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalInstant(player1, 0, INVENT, List.of());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch instantSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(instantSearch).isNotNull();
        assertThat(instantSearch.params().cards()).allMatch(card -> card.hasType(CardType.INSTANT));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch sorcerySearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(sorcerySearch).isNotNull();
        assertThat(sorcerySearch.params().cards()).allMatch(card -> card.hasType(CardType.SORCERY));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Shock");
        harness.assertInHand(player1, "Divination");
    }

    @Test
    @DisplayName("Fuse switches the targets and then performs Invent")
    void fuseResolvesBothHalves() {
        Permanent tortoise = harness.addToBattlefieldAndReturn(player2, new GiantTortoise());
        int power = tortoise.getEffectivePower();
        int toughness = tortoise.getEffectiveToughness();
        harness.setLibrary(player1, List.of(new Shock(), new Divination()));
        harness.setHand(player1, List.of(new InvertInvent()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalInstant(player1, 0, FUSE, List.of(tortoise.getId()));
        harness.passBothPriorities();

        assertThat(tortoise.getEffectivePower()).isEqualTo(toughness);
        assertThat(tortoise.getEffectiveToughness()).isEqualTo(power);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertInHand(player1, "Shock");
        harness.assertInHand(player1, "Divination");
    }
}
