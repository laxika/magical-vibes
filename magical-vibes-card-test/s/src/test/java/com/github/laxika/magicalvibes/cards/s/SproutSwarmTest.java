package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Delay;
import com.github.laxika.magicalvibes.cards.l.LlanowarAugur;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SproutSwarm.class, LlanowarAugur.class, Delay.class})
class SproutSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 green Saproling creature token")
    void createsSaprolingToken() {
        harness.castFromHand(player1, new SproutSwarm(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).singleElement().satisfies(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Paying buyback returns Sprout Swarm to hand as it resolves")
    void buybackReturnsToHand() {
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstantWithBuyback(player1, 0, null);
        assertThat(harness.getGameData().stack.getFirst().isBuyback()).isTrue();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Without buyback, Sprout Swarm goes to the graveyard after resolving")
    void withoutBuybackGoesToGraveyard() {
        SproutSwarm spell = new SproutSwarm();
        harness.castFromHand(player1, spell, "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Buyback requires all three additional mana")
    void buybackRequiresThreeAdditionalMana() {
        SproutSwarm spell = new SproutSwarm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithBuyback(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(harness.getGameData().playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Convoke taps a creature to help cast Sprout Swarm")
    void convokeTapsCreature() {
        Permanent convokeCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
        harness.setHand(player1, List.of(new SproutSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokeCreature.getId()));

        assertThat(convokeCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
    }

    @Test
    @DisplayName("Two summoning-sick green creatures can convoke the entire spell cost")
    void convokePaysEntireCostWithSummoningSickCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        SproutSwarm spell = new SproutSwarm();
        harness.setHand(player1, List.of(spell));

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Convoke can pay the spell cost and the additional buyback cost without mana")
    void convokePaysEntireCostIncludingBuyback() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new LlanowarAugur()))
                .toList();
        SproutSwarm spell = new SproutSwarm();
        harness.setHand(player1, List.of(spell));
        harness.ensurePriority(player1);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(),
                creatures.stream().map(Permanent::getId).toList(), false, null, null, null, null, null,
                false, null, null, null, null, List.of(), true);
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isTrue());
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped creature cannot help convoke Sprout Swarm")
    void tappedCreatureCannotConvoke() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
        creature.tap();
        SproutSwarm spell = new SproutSwarm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player1, 0, List.of(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Buyback does not return Sprout Swarm when Delay counters it")
    void counteredSpellDoesNotReturnWithBuyback() {
        SproutSwarm spell = new SproutSwarm();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstantWithBuyback(player1, 0, null);
        harness.setHand(player2, List.of(new Delay()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }
}
