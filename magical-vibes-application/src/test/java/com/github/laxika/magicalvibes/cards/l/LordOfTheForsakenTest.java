package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SqueeTheImmortal;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordOfTheForsaken.class, GrizzlyBears.class, SqueeTheImmortal.class})
class LordOfTheForsakenTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature mills the targeted player")
    void sacrificesAnotherCreatureAndMillsTargetPlayer() {
        addCreatureReady(player1, new LordOfTheForsaken());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The sacrifice ability cannot sacrifice Lord of the Forsaken itself")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new LordOfTheForsaken());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard-only mana casts a non-flashback spell from the graveyard")
    void graveyardOnlyManaCastsSpellFromGraveyard() {
        addCreatureReady(player1, new LordOfTheForsaken());
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.COLORLESS)).isEqualTo(1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Squee, the Immortal");
        assertThat(gd.playerManaPools.get(player1.getId()).getGraveyardOnlyMana(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Graveyard-only mana cannot pay for a spell cast from hand")
    void graveyardOnlyManaCannotPayForSpellFromHand() {
        addCreatureReady(player1, new LordOfTheForsaken());
        harness.setHand(player1, List.of(new SqueeTheImmortal()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
