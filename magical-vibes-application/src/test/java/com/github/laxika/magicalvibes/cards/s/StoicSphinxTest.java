package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoicSphinx.class, Shock.class})
class StoicSphinxTest extends BaseCardTest {

    @Test
    void hasHexproofBeforeItsControllerCastsASpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void losesHexproofAfterItsControllerCastsASpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void doesNotHaveHexproofWhenItWasCastThisTurn() {
        harness.setHand(player1, java.util.List.of(new StoicSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sphinx = findPermanent(player1, "Stoic Sphinx");
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void losesHexproofAsSoonAsItsControllerCastsASpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void keepsHexproofWhenOnlyItsOpponentCastsASpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void cannotBeTargetedByAnOpponentBeforeItsControllerCasts() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());
        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeTargetedByItsControllerBeforeCastingAnySpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, sphinx.getId());

        assertThat(sphinx.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void regainsHexproofAtTheStartOfTheNextTurn() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void canBeCastOnAnOpponentsTurnButDoesNotHaveHexproofThatTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, java.util.List.of(new StoicSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sphinx = findPermanent(player1, "Stoic Sphinx");
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();
    }
}
