package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelOfJubilation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlumbTheForbidden.class, AngelOfJubilation.class, Forest.class, GrizzlyBears.class,
        Island.class})
class PlumbTheForbiddenTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and loses life once when no creatures are sacrificed")
    void resolvesNormallyWithoutSacrifice() {
        setupLibrary(1);
        prepareSpell();

        cast(List.of());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Creates one copy per creature sacrificed")
    void copiesForEachSacrificedCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupLibrary(3);
        prepareSpell();

        cast(List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Plumb the Forbidden");
    }

    @Test
    @DisplayName("Rejects a noncreature chosen for the optional sacrifice cost")
    void rejectsNoncreatureSacrifice() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        setupLibrary(1);
        prepareSpell();

        assertThatThrownBy(() -> cast(List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Cannot sacrifice creatures as an additional cost under Angel of Jubilation")
    void respectsCreatureSacrificeRestriction() {
        harness.addToBattlefieldAndReturn(player1, new AngelOfJubilation());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        setupLibrary(1);
        prepareSpell();

        assertThatThrownBy(() -> cast(List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice creatures");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new PlumbTheForbidden()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void cast(List<UUID> sacrificeIds) {
        harness.castSorceryWithSacrifices(player1, 0, null, sacrificeIds);
    }

    private void setupLibrary(int count) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        for (int i = 0; i < count; i++) {
            deck.add(new Island());
        }
    }
}
