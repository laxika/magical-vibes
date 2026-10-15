package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PrecognitionField;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WranglerOfTheDamned.class, GrizzlyBears.class, PrecognitionField.class, Shock.class})
class WranglerOfTheDamnedTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 2/2 white Spirit token with flying when no spell was cast from hand")
    void createsSpiritWhenNoSpellWasCastFromHand() {
        addWrangler();

        advanceToEndStep(player1);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not create a token after a spell was cast from hand")
    void doesNotCreateSpiritAfterHandSpell() {
        addWrangler();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Creates a token when the only spell cast was from the library")
    void createsSpiritAfterLibrarySpell() {
        addWrangler();
        harness.addToBattlefield(player1, new PrecognitionField());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveFromLibraryTop(player1, player2.getId());
        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addWrangler();

        advanceToEndStep(player2);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void opponentsHandSpellDoesNotPreventToken() {
        addWrangler();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void handSpellInResponsePreventsTokenOnResolution() {
        addWrangler();
        harness.setHand(player1, List.of(new WranglerOfTheDamned()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wrangler of the Damned")).hasSize(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void castingWranglerFromHandCountsAsHandSpell() {
        harness.setHand(player1, List.of(new WranglerOfTheDamned()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(findPermanents(player1, "Wrangler of the Damned")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void eachWranglerCreatesItsOwnToken() {
        addWrangler();
        addWrangler();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player1, "Spirit")).allSatisfy(spirit -> {
            assertThat(spirit.getCard().isToken()).isTrue();
            assertThat(spirit.getCard().getColors()).containsExactly(CardColor.WHITE);
            assertThat(spirit.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
            assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(2);
            assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
        });
    }

    private void addWrangler() {
        harness.addToBattlefield(player1, new WranglerOfTheDamned());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
