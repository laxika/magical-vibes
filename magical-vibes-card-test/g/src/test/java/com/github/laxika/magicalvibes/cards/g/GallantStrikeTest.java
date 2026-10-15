package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WreckageWickerfolk;
import com.github.laxika.magicalvibes.cards.b.BroadcastRambler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GallantStrike.class, Forest.class, WreckageWickerfolk.class, BroadcastRambler.class})
class GallantStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature with toughness 4 or greater")
    void destroysCreatureWithEnoughToughness() {
        Permanent creature = addCreature(player2, "Large Creature", 4);

        castGallantStrike(creature);

        harness.assertNotOnBattlefield(player2, "Large Creature");
        harness.assertInGraveyard(player2, "Large Creature");
    }

    @Test
    @DisplayName("Cannot target a creature with toughness less than 4")
    void cannotTargetCreatureWithLowToughness() {
        Permanent creature = addCreature(player2, "Small Creature", 3);

        assertThatThrownBy(() -> castGallantStrike(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Gallant Strike and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new GallantStrike()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gallant Strike");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cycling discards as a cost before drawing on resolution")
    void cyclingDiscardsBeforeResolution() {
        harness.setHand(player1, List.of(new GallantStrike()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Gallant Strike");
        harness.assertInGraveyard(player1, "Gallant Strike");
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Cycling requires two mana and does not discard when payment fails")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new GallantStrike()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Gallant Strike");
        harness.assertNotInGraveyard(player1, "Gallant Strike");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Uses modified toughness when selecting a target")
    void canTargetCreatureRaisedToFourToughness() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WreckageWickerfolk());
        creature.setToughnessModifier(1);

        castGallantStrike(creature);

        harness.assertNotOnBattlefield(player2, "Wreckage Wickerfolk");
        harness.assertInGraveyard(player2, "Wreckage Wickerfolk");
    }

    @Test
    @DisplayName("Target survives when toughness falls below four before resolution")
    void rechecksToughnessOnResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WreckageWickerfolk());
        creature.setToughnessModifier(1);
        harness.setHand(player1, List.of(new GallantStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());

        creature.setToughnessModifier(0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Wreckage Wickerfolk");
        harness.assertNotInGraveyard(player2, "Wreckage Wickerfolk");
        harness.assertInGraveyard(player1, "Gallant Strike");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an uncrewed Vehicle with printed toughness four")
    void cannotTargetNoncreatureVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new BroadcastRambler());

        assertThatThrownBy(() -> castGallantStrike(vehicle))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Broadcast Rambler");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by its caster")
    void canDestroyOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WreckageWickerfolk());
        creature.setToughnessModifier(1);

        castGallantStrike(creature);

        harness.assertNotOnBattlefield(player1, "Wreckage Wickerfolk");
        harness.assertInGraveyard(player1, "Wreckage Wickerfolk");
    }

    private void castGallantStrike(Permanent target) {
        harness.setHand(player1, List.of(new GallantStrike()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private Permanent addCreature(Player player, String name, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
