package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MisthollowGriffin.class})
class MisthollowGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolves onto the battlefield when cast from hand")
    void castFromHand() {
        harness.castFromHand(player1, new MisthollowGriffin(), "{2}{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Misthollow Griffin");
    }

    @Test
    @DisplayName("Can be cast from exile and leaves the exile zone")
    void castFromExile() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        harness.setExile(player1, List.of(griffin));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, griffin.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Misthollow Griffin");
    }

    @Test
    @DisplayName("Casting from exile requires sorcery-speed timing")
    void exileCastRequiresSorceryTiming() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        harness.setExile(player1, List.of(griffin));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromExile(player1, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exileCastRequiresFullManaCostAndCanBeRetried() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        harness.setExile(player1, List.of(griffin));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(griffin);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, griffin.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Misthollow Griffin");
    }

    @Test
    void cannotCastOpponentsGriffinFromExile() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        harness.setExile(player2, List.of(griffin));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(griffin);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastUnseenFaceDownGriffinFromExile() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        gd.addToExile(player1.getId(), griffin, null, true);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(griffin.getId())).isNotNull();
        assertThat(gd.findExiledCard(griffin.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCastFromExileWithSpellOnStack() {
        MisthollowGriffin griffin = new MisthollowGriffin();
        harness.setExile(player1, List.of(griffin));
        harness.castFromHand(player1, new MisthollowGriffin(), "{2}{U}{U}");
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, griffin.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(griffin);
        assertThat(gd.stack).hasSize(1);
    }
}
