package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ContainmentPriest;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NurglesConscription.class, GrizzlyBears.class, Pacifism.class,
        GrafdiggersCage.class, ContainmentPriest.class})
class NurglesConscriptionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a tapped opponent creature and exiles that player's remaining graveyard")
    void returnsTappedCreatureAndExilesGraveyard() {
        Card creature = new GrizzlyBears();
        Card otherCard = new Pacifism();
        harness.setGraveyard(player2, List.of(creature, otherCard));
        harness.setHand(player1, List.of(new NurglesConscription()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCard().getId()).isEqualTo(creature.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(otherCard);
    }

    @Test
    @DisplayName("Requires a creature card in an opponent's graveyard")
    void rejectsInvalidTarget() {
        Card target = new Pacifism();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new NurglesConscription()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    @DisplayName("Fizzles if the targeted creature card leaves the graveyard")
    void fizzlesWhenTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        Card otherCard = new Pacifism();
        harness.setGraveyard(player2, List.of(creature, otherCard));
        harness.setHand(player1, List.of(new NurglesConscription()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0, creature.getId());
        harness.setGraveyard(player2, List.of(otherCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature in your own graveyard")
    void rejectsOwnGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NurglesConscription()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returning the only opponent graveyard card leaves your graveyard untouched")
    void returnsOnlyCardWithoutExilingOwnGraveyard() {
        Card creature = new GrizzlyBears();
        Card ownCard = new Pacifism();
        harness.setGraveyard(player2, List.of(creature));
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setHand(player1, List.of(new NurglesConscription()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents reanimation but the whole opponent graveyard is still exiled")
    void exilesGraveyardWhenReanimationIsProhibited() {
        Card creature = new GrizzlyBears();
        Card otherCard = new Pacifism();
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setGraveyard(player2, List.of(creature, otherCard));
        harness.setHand(player1, List.of(new NurglesConscription()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(creature, otherCard);
    }

    @Test
    @DisplayName("Replacing the creature's entry with exile does not stop exiling the rest of its owner's graveyard")
    void exilesGraveyardWhenCreatureEntryIsReplaced() {
        Card creature = new GrizzlyBears();
        creature.setOwnerId(player2.getId());
        Card otherCard = new Pacifism();
        harness.addToBattlefield(player1, new ContainmentPriest());
        harness.setGraveyard(player2, List.of(creature, otherCard));
        harness.setHand(player1, List.of(new NurglesConscription()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(creature, otherCard);
    }
}
