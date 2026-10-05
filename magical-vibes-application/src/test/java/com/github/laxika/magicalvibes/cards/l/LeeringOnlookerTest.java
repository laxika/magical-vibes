package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed(LeeringOnlooker.class)
class LeeringOnlookerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling Leering Onlooker creates two tapped flying Bat tokens")
    void graveyardAbilityCreatesTappedFlyingBats() {
        harness.setGraveyard(player1, List.of(new LeeringOnlooker()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bats = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(bats).hasSize(2);
        assertThat(bats).allSatisfy(bat -> {
            assertThat(bat.isTapped()).isTrue();
            assertThat(bat.getEffectivePower()).isEqualTo(1);
            assertThat(bat.getEffectiveToughness()).isEqualTo(1);
            assertThat(bat.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(bat.getCard().getSubtypes()).contains(CardSubtype.BAT);
            assertThat(bat.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        harness.assertNotInGraveyard(player1, "Leering Onlooker");
    }

    @Test
    @DisplayName("Exile and mana are paid before the Bat tokens resolve")
    void paysCostsImmediately() {
        LeeringOnlooker onlooker = new LeeringOnlooker();
        harness.setGraveyard(player1, List.of(onlooker));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Leering Onlooker");
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(onlooker.getId());
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Four mana with only one black mana cannot pay the activation cost")
    void requiresTwoBlackMana() {
        harness.setGraveyard(player1, List.of(new LeeringOnlooker()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Leering Onlooker");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The graveyard ability can be activated during an opponent's end step")
    void activatesDuringOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new LeeringOnlooker()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allSatisfy(bat -> assertThat(bat.isTapped()).isTrue());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Leering Onlooker");
    }
}
