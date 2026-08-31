package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed(SystemsOverride.class)
class SystemsOverrideTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, and grants haste to an artifact or creature")
    void controlsUntapsAndGrantsHaste() {
        Permanent target = addPermanent(player2, "Target Artifact", CardType.ARTIFACT);
        target.tap();

        cast(target);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Puts ten charge counters on a Spacecraft and removes them at the next end step")
    void addsAndRemovesSpacecraftCounters() {
        Permanent spacecraft = addPermanent(player2, "Target Spacecraft", CardType.ARTIFACT,
                CardSubtype.SPACECRAFT);
        spacecraft.setCounterCount(CounterType.CHARGE, 2);

        cast(spacecraft);

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(12);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(spacecraft.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not put charge counters on a non-Spacecraft artifact")
    void doesNotAddCountersToNonSpacecraft() {
        Permanent artifact = addPermanent(player2, "Target Artifact", CardType.ARTIFACT);

        cast(artifact);

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetOtherPermanent() {
        Permanent enchantment = addPermanent(player2, "Target Enchantment", CardType.ENCHANTMENT);

        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    @DisplayName("Returns the permanent and removes haste at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addPermanent(player2, "Target Creature", CardType.CREATURE);

        cast(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    private void cast(Permanent target) {
        prepareCast();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new SystemsOverride()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addPermanent(com.github.laxika.magicalvibes.model.Player player,
                                   String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
