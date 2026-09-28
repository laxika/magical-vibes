package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FallajiWayfarer.class, GrizzlyBears.class})
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

        UUID convokerId = convoker.getId();
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokerId));

        assertThat(convoker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Monocolored spells do not gain convoke")
    void doesNotGrantConvokeToMonocoloredSpells() {
        harness.addToBattlefield(player1, new FallajiWayfarer());
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(createSpell("Monocolored spell", List.of(CardColor.BLUE))));

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
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
