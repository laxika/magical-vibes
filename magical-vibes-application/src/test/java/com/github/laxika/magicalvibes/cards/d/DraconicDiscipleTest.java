package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DraconicDisciple.class})
class DraconicDiscipleTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Draconic Disciple prompts for an any-color mana choice")
    void tappingProducesAnyColorMana() {
        Permanent disciple = addCreatureReady(player1, new DraconicDisciple());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying seven mana and sacrificing Draconic Disciple creates a 5/5 flying Dragon")
    void sacrificesToCreateDragonToken() {
        addCreatureReady(player1, new DraconicDisciple());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Draconic Disciple");
        harness.passBothPriorities();

        List<Permanent> dragons = findPermanents(player1, "Dragon");
        assertThat(dragons).hasSize(1);
        Permanent dragon = dragons.getFirst();
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(5);
        assertThat(dragon.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(dragon.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(dragon.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(dragon.getCard().isToken()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canChooseEachManaColor(ManaColor color) {
        Permanent disciple = addCreatureReady(player1, new DraconicDisciple());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(disciple.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor poolColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(poolColor))
                    .isEqualTo(poolColor == color ? 1 : 0);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void summoningSicknessPreventsBothAbilities(int abilityIndex) {
        Permanent disciple = harness.addToBattlefieldAndReturn(player1, new DraconicDisciple());
        disciple.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");

        assertThat(disciple.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Draconic Disciple");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(7);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void tappedDiscipleCannotActivateEitherAbility(int abilityIndex) {
        Permanent disciple = addCreatureReady(player1, new DraconicDisciple());
        disciple.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Draconic Disciple");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(7);
    }

    @Test
    void insufficientManaDoesNotTapOrSacrificeDisciple() {
        Permanent disciple = addCreatureReady(player1, new DraconicDisciple());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Not enough mana");

        assertThat(disciple.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Draconic Disciple");
        harness.assertNotInGraveyard(player1, "Draconic Disciple");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(6);
    }

    @Test
    void sacrificeAndManaArePaidBeforeDragonAbilityResolves() {
        Permanent disciple = addCreatureReady(player1, new DraconicDisciple());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(disciple.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Draconic Disciple");
        harness.assertNotOnBattlefield(player1, "Dragon");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Dragon")).hasSize(1);
        assertThat(findPermanents(player2, "Dragon")).isEmpty();
    }
}
