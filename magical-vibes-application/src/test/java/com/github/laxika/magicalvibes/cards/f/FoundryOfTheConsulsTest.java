package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({FoundryOfTheConsuls.class})
class FoundryOfTheConsulsTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds one colorless")
    void tapsForColorless() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrifice ability creates two 1/1 flying Thopters")
    void createsTwoThopters() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Foundry of the Consuls");
        harness.assertInGraveyard(player1, "Foundry of the Consuls");

        List<Permanent> thopters = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Thopter"))
                .toList();
        assertThat(thopters).hasSize(2);
        assertThat(thopters).allSatisfy(t -> {
            assertThat(t.getCard().getPower()).isEqualTo(1);
            assertThat(t.getCard().getToughness()).isEqualTo(1);
            assertThat(t.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated without enough mana")
    void requiresFiveMana() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertOnBattlefield(player1, "Foundry of the Consuls");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thopter"));
    }

    @Test
    void sacrificeIsPaidBeforeTokensAreCreated() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertInGraveyard(player1, "Foundry of the Consuls");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().getColor()).isNull();
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
            assertThat(token.isTapped()).isFalse();
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void cannotSacrificeAfterTappingForMana() {
        harness.addToBattlefield(player1, new FoundryOfTheConsuls());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .matches(Permanent::isTapped);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Foundry of the Consuls");
        harness.assertNotInGraveyard(player1, "Foundry of the Consuls");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }
}
