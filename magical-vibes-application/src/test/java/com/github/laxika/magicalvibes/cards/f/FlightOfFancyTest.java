package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.b.BorosSignet;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlightOfFancy.class, BorosRecruit.class, BorosSignet.class})
class FlightOfFancyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Flight of Fancy attaches it, draws two cards, and grants flying")
    void resolvingAttachesDrawsTwoAndGrantsFlying() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        harness.setHand(player1, List.of(new FlightOfFancy()));
        harness.setLibrary(player1, List.of(new BorosRecruit(), new BorosRecruit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, recruit.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Flight of Fancy")
                        && recruit.getId().equals(p.getAttachedTo()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Removing Flight of Fancy removes flying")
    void effectsStopWhenRemoved() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player1, new BorosRecruit());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FlightOfFancy());
        aura.setAttachedTo(recruit.getId());

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flight of Fancy can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent recruit = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());

        harness.setHand(player1, List.of(new FlightOfFancy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, recruit.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Flight of Fancy")
                        && recruit.getId().equals(p.getAttachedTo()));
        assertThat(gqs.hasKeyword(gd, recruit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flight of Fancy cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BorosSignet());
        harness.setHand(player1, List.of(new FlightOfFancy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
