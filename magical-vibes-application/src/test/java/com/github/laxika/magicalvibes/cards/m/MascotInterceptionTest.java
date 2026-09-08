package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MascotInterception.class, GrizzlyBears.class})
class MascotInterceptionTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {R} when targeting a creature token")
    void reducedCostForTokenTarget() {
        Permanent token = addToken(player2);
        token.tap();

        harness.setHand(player1, List.of(new MascotInterception()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Requires the full cost when targeting a nontoken creature")
    void fullCostForNontokenTarget() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MascotInterception()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Steals, untaps, boosts, and hastes the target creature")
    void resolvesAllEffects() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        harness.setHand(player1, List.of(new MascotInterception()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Control, boost, and haste expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent token = addToken(player2);

        harness.setHand(player1, List.of(new MascotInterception()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, token.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Card enchantmentCard = new Card();
        enchantmentCard.setName("Test Enchantment");
        enchantmentCard.setType(CardType.ENCHANTMENT);
        enchantmentCard.setManaCost("{1}");
        enchantmentCard.setColor(CardColor.BLUE);
        Permanent enchantment = new Permanent(enchantmentCard);
        gd.playerBattlefields.get(player2.getId()).add(enchantment);

        harness.setHand(player1, List.of(new MascotInterception()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player controller) {
        Card tokenCard = new Card();
        tokenCard.setName("Test Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setColor(CardColor.RED);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setToken(true);
        return addCreatureReady(controller, tokenCard);
    }
}
