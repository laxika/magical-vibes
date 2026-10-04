package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrowthSpiral;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallajiWayfarer.class, GrizzlyBears.class, GrowthSpiral.class, Cultivate.class, MycosynthLattice.class})
class FallajiWayfarerTest extends BaseCardTest {

    @Test
    @DisplayName("Fallaji Wayfarer is all five colors")
    void isAllColors() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new FallajiWayfarer());

        assertThat(gqs.getEffectiveColors(gd, wayfarer)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("Multicolored spells can be cast using convoke")
    void grantsConvokeToMulticoloredSpells() {
        harness.addToBattlefield(player1, new FallajiWayfarer());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(createSpell("Multicolored spell", List.of(CardColor.BLUE, CardColor.RED))));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));

        assertThat(convoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Monocolored spells do not gain convoke")
    void doesNotGrantConvokeToMonocoloredSpells() {
        harness.addToBattlefield(player1, new FallajiWayfarer());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(createSpell("Monocolored spell", List.of(CardColor.BLUE))));

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Wayfarer can pay blue for Growth Spiral despite its green mana cost")
    void wayfarerCanConvokeForBlue() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new FallajiWayfarer());
        harness.setHand(player1, List.of(new GrowthSpiral()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(wayfarer.getId()));

        assertThat(wayfarer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A second Wayfarer is a multicolored spell and can be convoked")
    void grantsConvokeToAnotherWayfarer() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new FallajiWayfarer());
        harness.setHand(player1, List.of(new FallajiWayfarer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(wayfarer.getId()));

        assertThat(wayfarer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cultivate does not gain convoke")
    void realMonocoloredSpellDoesNotGainConvoke() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new FallajiWayfarer());
        harness.setHand(player1, List.of(new Cultivate()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(wayfarer.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wayfarer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Wayfarer does not grant convoke")
    void opponentsWayfarerDoesNotGrantConvoke() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player2, new FallajiWayfarer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new GrowthSpiral()));
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
        assertThat(wayfarer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless spells under Mycosynth Lattice do not gain convoke")
    void colorlessSpellDoesNotGainConvokeUnderLattice() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new FallajiWayfarer());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new FallajiWayfarer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(wayfarer.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(wayfarer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Card createSpell(String name, List<CardColor> colors) {
        Card spell = new Card();
        spell.setName(name);
        spell.setType(CardType.INSTANT);
        spell.setManaCost("{1}{U}");
        spell.setColors(colors);
        return spell;
    }
}
