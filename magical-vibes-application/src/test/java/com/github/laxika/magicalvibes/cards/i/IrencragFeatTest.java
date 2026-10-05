package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IrencragFeat.class, Shock.class})
class IrencragFeatTest extends BaseCardTest {

    @Test
    @DisplayName("Restriction starts when Irencrag Feat resolves")
    void restrictionStartsOnResolution() {
        harness.setHand(player1, List.of(new IrencragFeat()));
        addFeatMana();

        harness.castSorcery(player1, 0, 0);
        assertThat(gd.playersMaxSpellsThisTurn).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playersMaxSpellsThisTurn).containsEntry(player1.getId(), 2);
    }

    @Test
    @DisplayName("Resolving adds seven red mana")
    void resolvingAddsSevenRedMana() {
        harness.setHand(player1, List.of(new IrencragFeat()));
        addFeatMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(7);
    }

    @Test
    @DisplayName("Controller can cast exactly one more spell this turn")
    void controllerCanCastExactlyOneMoreSpell() {
        harness.setHand(player1, List.of(new IrencragFeat(), new Shock(), new Shock()));
        addFeatMana();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restriction is cleared at end of turn")
    void restrictionIsClearedAtEndOfTurn() {
        gd.playersMaxSpellsThisTurn.put(player1.getId(), 2);

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gd.playersMaxSpellsThisTurn).isEmpty();
    }

    @Test
    @DisplayName("Spells cast before Feat resolves do not consume its additional spell")
    void spellsBeforeResolutionDoNotConsumeAdditionalSpell() {
        harness.setHand(player1, List.of(new IrencragFeat(), new Shock(), new Shock(), new Shock()));
        addFeatMana();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 16);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A second Feat does not grant another spell beyond the first restriction")
    void secondFeatDoesNotExtendFirstRestriction() {
        harness.setHand(player1, List.of(new IrencragFeat(), new IrencragFeat(), new Shock()));
        addFeatMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(10);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Feat does not restrict the opponent's spell casts")
    void opponentCanCastMultipleSpells() {
        harness.setHand(player1, List.of(new IrencragFeat()));
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        addFeatMana();
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 16);
    }

    private void addFeatMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 3);
    }
}
