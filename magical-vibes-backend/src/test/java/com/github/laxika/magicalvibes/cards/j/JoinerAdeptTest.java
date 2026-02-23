package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JoinerAdeptTest extends BaseCardTest {


    @Test
    @DisplayName("Joiner Adept has correct card properties")
    void hasCorrectProperties() {
        JoinerAdept card = new JoinerAdept();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(GrantActivatedAbilityEffect.class);
    }

    @Test
    @DisplayName("Lands you control gain tap ability to add one mana of any color")
    void ownLandsGainAnyColorManaAbility() {
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player1, new Forest());

        Permanent forest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst().orElseThrow();
        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);

        harness.activateAbility(player1, forestIndex, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);

        harness.handleColorChosen(player1, "RED");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lands do not have the granted ability without Joiner Adept")
    void landsDoNotHaveAbilityWithoutJoinerAdept() {
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Opponent lands do not gain Joiner Adept ability")
    void opponentLandsDoNotGainAbility() {
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("Granted ability is lost when Joiner Adept leaves battlefield")
    void grantedAbilityLostWhenJoinerAdeptLeaves() {
        harness.addToBattlefield(player1, new JoinerAdept());
        harness.addToBattlefield(player1, new Forest());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Joiner Adept"));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }
}
