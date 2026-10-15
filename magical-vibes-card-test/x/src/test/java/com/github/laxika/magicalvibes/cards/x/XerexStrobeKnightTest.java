package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({XerexStrobeKnight.class, Shock.class})
class XerexStrobeKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate before casting two spells this turn")
    void cannotActivateBeforeTwoSpells() {
        Permanent knight = addReadyKnight(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more spells");
        assertThat(knight.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creates a vigilant 2/2 white and blue Knight after two spells")
    void createsKnightAfterTwoSpells() {
        addReadyKnight(player1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getSubtypes())
                .contains(com.github.laxika.magicalvibes.model.CardSubtype.KNIGHT);
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without casting any spells")
    void cannotActivateWithoutSpells() {
        Permanent knight = addReadyKnight(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more spells");
        assertThat(knight.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second spell counts before it resolves and activation pays the tap cost")
    void secondSpellCountsBeforeResolution() {
        Permanent knight = addReadyKnight(player1);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(knight.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An opponent's spells do not satisfy the activation restriction")
    void opponentSpellsDoNotCount() {
        Permanent knight = addReadyKnight(player1);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more spells");
        assertThat(knight.isTapped()).isFalse();
    }

    @Test
    @CardUsed({XerexStrobeKnight.class})
    @DisplayName("Creature spells count, but a newly cast Knight cannot pay a tap cost")
    void creatureSpellsCountButSummoningSicknessPreventsActivation() {
        addReadyKnight(player1);
        harness.setHand(player1, List.of(new XerexStrobeKnight(), new XerexStrobeKnight()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList()).hasSize(1);
    }

    private Permanent addReadyKnight(Player player) {
        return addCreatureReady(player, new XerexStrobeKnight());
    }
}
