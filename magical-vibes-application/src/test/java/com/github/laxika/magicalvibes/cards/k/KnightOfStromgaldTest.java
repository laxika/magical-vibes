package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfStromgald.class, KjeldoranWarrior.class, SwordsToPlowshares.class})
class KnightOfStromgaldTest extends BaseCardTest {

    @Test
    @DisplayName("Knight of Stromgald has protection from white")
    void hasProtectionFromWhite() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());

        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, knight, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection from white prevents white spells from targeting Knight of Stromgald")
    void cannotBeTargetedByWhiteSpell() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());
        addCreatureReady(player1, new KjeldoranWarrior());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, knight.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("White creatures cannot block Knight of Stromgald")
    void whiteCreatureCannotBlock() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());
        knight.setAttacking(true);
        addCreatureReady(player2, new KjeldoranWarrior());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    // ===== First strike ability =====

    @Test
    @DisplayName("Resolving first ability grants first strike until end of turn")
    void firstAbilityGrantsFirstStrike() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike granted by ability resets at end of turn cleanup")
    void firstStrikeResetsAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate first ability without black mana")
    void cannotActivateFirstStrikeWithoutMana() {
        addCreatureReady(player1, new KnightOfStromgald());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== +1/+0 ability =====

    @Test
    @DisplayName("Resolving second ability gives +1/+0 until end of turn")
    void secondAbilityBoostsPower() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(knight.getPowerModifier()).isEqualTo(1);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate second ability with only one black mana")
    void cannotActivateBoostWithoutEnoughMana() {
        addCreatureReady(player1, new KnightOfStromgald());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Neither activated ability requires Knight of Stromgald to be untapped")
    void abilitiesCanBeActivatedWhileTapped() {
        Permanent knight = addCreatureReady(player1, new KnightOfStromgald());
        knight.tap();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(knight.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(knight.getPowerModifier()).isEqualTo(1);
    }
}
