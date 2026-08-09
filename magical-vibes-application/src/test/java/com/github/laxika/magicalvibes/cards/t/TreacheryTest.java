package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TreacheryTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Treachery gains control of the enchanted creature")
    void gainsControlOfEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Treachery()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Treachery")
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("When Treachery enters, it offers up to five tapped lands from any battlefield")
    void offersUpToFiveTappedLandsFromAnyBattlefield() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        List<Permanent> ownLands = addTappedLands(player1, 3);
        List<Permanent> opposingLands = addTappedLands(player2, 3);

        harness.setHand(player1, List.of(new Treachery()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(5);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                ownLands.get(0).getId(), ownLands.get(1).getId(), ownLands.get(2).getId(),
                opposingLands.get(0).getId(), opposingLands.get(1).getId(), opposingLands.get(2).getId());

        harness.handleMultiplePermanentsChosen(player1, choice.validIds().subList(0, 5));

        assertThat(ownLands).allMatch(p -> !p.isTapped());
        assertThat(opposingLands).filteredOn(Permanent::isTapped).hasSize(1);
    }

    @Test
    @DisplayName("Treachery does not offer non-land permanents to untap")
    void doesNotOfferNonLands() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingLand = addTappedLands(player2, 1).getFirst();
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        opposingCreature.tap();

        harness.setHand(player1, List.of(new Treachery()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(opposingLand.getId());
    }

    @Test
    @DisplayName("Treachery can target only a creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.setHand(player1, List.of(new Treachery()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private List<Permanent> addTappedLands(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Permanent> lands = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player, new Forest());
            land.tap();
            lands.add(land);
        }
        return lands;
    }
}
