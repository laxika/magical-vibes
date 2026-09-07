package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WarpingWail.class, Divination.class, GoblinPiker.class, GrizzlyBears.class, Ornithopter.class})
class WarpingWailTest extends BaseCardTest {

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    @Nested
    @DisplayName("Exile mode")
    class ExileMode {

        @Test
        @DisplayName("Exiles a creature with power or toughness 1 or less")
        void exilesSmallCreature() {
            GoblinPiker target = new GoblinPiker();
            var targetPermanent = harness.addToBattlefieldAndReturn(player2, target);
            harness.setHand(player1, List.of(new WarpingWail()));
            addMana(player1);

            harness.castInstant(player1, 0, 0, targetPermanent.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(permanent -> permanent.getCard() == target);
            assertThat(gd.getPlayerExiledCards(player2.getId()))
                    .anyMatch(card -> card.getId().equals(target.getId()));
        }

        @Test
        @DisplayName("Also exiles a creature with power 1 or less")
        void exilesLowPowerCreature() {
            Ornithopter target = new Ornithopter();
            var targetPermanent = harness.addToBattlefieldAndReturn(player2, target);
            harness.setHand(player1, List.of(new WarpingWail()));
            addMana(player1);

            harness.castInstant(player1, 0, 0, targetPermanent.getId());
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(permanent -> permanent.getCard() == target);
        }

        @Test
        @DisplayName("Cannot target a creature with both power and toughness greater than 1")
        void rejectsLargeCreature() {
            GrizzlyBears target = new GrizzlyBears();
            var targetPermanent = harness.addToBattlefieldAndReturn(player2, target);
            harness.setHand(player1, List.of(new WarpingWail()));
            addMana(player1);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetPermanent.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("Counters a sorcery spell")
    void countersSorcery() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(new WarpingWail()));
        addMana(player2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, divination.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(divination);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-sorcery spell")
    void rejectsNonSorcerySpell() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new WarpingWail()));
        addMana(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates an Eldrazi Scion token that can be sacrificed for colorless mana")
    void createsSacrificeForManaToken() {
        harness.setHand(player1, List.of(new WarpingWail()));
        addMana(player1);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        var scion = gd.playerBattlefields.get(player1.getId()).getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scion);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
