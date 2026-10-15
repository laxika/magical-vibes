package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CradleToGrave;
import com.github.laxika.magicalvibes.cards.d.DismalFailure;
import com.github.laxika.magicalvibes.cards.h.Harmonize;
import com.github.laxika.magicalvibes.cards.s.SerraSphinx;
import com.github.laxika.magicalvibes.cards.w.WistfulThinking;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImpsMischief.class, CradleToGrave.class, SerraSphinx.class, Harmonize.class,
        WistfulThinking.class, DismalFailure.class})
class ImpsMischiefTest extends BaseCardTest {

    @Test
    void canRedirectCounterspellToResolvingMischief() {
        Harmonize harmonize = new Harmonize();
        harness.setLibrary(player1, List.of(new SerraSphinx(), new SerraSphinx(), new SerraSphinx()));
        harness.castFromHand(player1, harmonize, "{2}{G}{G}");
        harness.passPriority(player1);

        DismalFailure counter = new DismalFailure();
        harness.setHand(player2, List.of(counter));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, harmonize.getId());
        harness.passPriority(player2);

        ImpsMischief mischief = new ImpsMischief();
        harness.setHand(player1, List.of(mischief));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, counter.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mischief.getId())
                .doesNotContain(counter.getId(), harmonize.getId());

        harness.handlePermanentChosen(player1, mischief.getId());
        harness.assertLife(player1, 16);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Imp's Mischief");
        harness.assertInGraveyard(player1, "Harmonize");
        harness.assertInGraveyard(player2, "Dismal Failure");
    }

    @Test
    void doesNotLoseLifeWhenTargetSpellIsCounteredBeforeResolution() {
        UUID sphinxId = harness.enterBattlefieldAndReturn(player1, new SerraSphinx()).getId();
        CradleToGrave cradle = new CradleToGrave();
        harness.setHand(player1, List.of(cradle));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, sphinxId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, cradle.getId());

        harness.setHand(player1, List.of(new DismalFailure()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, cradle.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Serra Sphinx");
        harness.assertInGraveyard(player1, "Cradle to Grave");
        harness.assertInGraveyard(player2, "Imp's Mischief");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void retargetsSingleTargetSpellAndLosesLifeEqualToManaValue() {
        UUID sphinx1PermId = harness.enterBattlefieldAndReturn(player1, new SerraSphinx()).getId();
        UUID sphinx2PermId = harness.enterBattlefieldAndReturn(player2, new SerraSphinx()).getId();

        CradleToGrave cradle = new CradleToGrave();
        harness.setHand(player1, List.of(cradle));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, sphinx1PermId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, cradle.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(sphinx2PermId)
                .doesNotContain(sphinx1PermId);

        harness.handlePermanentChosen(player2, sphinx2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(sphinx1PermId));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(sphinx2PermId));
    }

    @Test
    void stillLosesLifeWhenThereIsNoLegalNewTarget() {
        UUID sphinxPermId = harness.enterBattlefieldAndReturn(player1, new SerraSphinx()).getId();

        CradleToGrave cradle = new CradleToGrave();
        harness.setHand(player1, List.of(cradle));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, sphinxPermId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, cradle.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(sphinxPermId);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(sphinxPermId));
    }

    @Test
    void retargetsEvenWhenOriginalTargetBecomesIllegal() {
        UUID sphinx1PermId = harness.enterBattlefieldAndReturn(player1, new SerraSphinx()).getId();
        UUID sphinx2PermId = harness.enterBattlefieldAndReturn(player2, new SerraSphinx()).getId();

        CradleToGrave firstCradle = new CradleToGrave();
        CradleToGrave secondCradle = new CradleToGrave();
        harness.setHand(player1, List.of(firstCradle, secondCradle));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, sphinx1PermId);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, firstCradle.getId());

        harness.castInstant(player1, 0, sphinx1PermId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(sphinx2PermId)
                .doesNotContain(sphinx1PermId);

        harness.handlePermanentChosen(player2, sphinx2PermId);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(sphinx1PermId));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(sphinx2PermId));
    }

    @Test
    void castingRequiresTargetingSingleTargetSpell() {
        Harmonize harmonize = new Harmonize();
        harness.castFromHand(player1, harmonize, "{2}{G}{G}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ImpsMischief()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harmonize.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single target");
    }

    @Test
    void retargetsPlayerTargetSpellAndLosesItsManaValue() {
        WistfulThinking wistfulThinking = new WistfulThinking();
        harness.setHand(player1, List.of(wistfulThinking));
        harness.setHand(player2, List.of(
                new ImpsMischief(), new SerraSphinx(), new SerraSphinx(), new SerraSphinx()));
        harness.setLibrary(player2, List.of(new SerraSphinx(), new SerraSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passPriority(player1);

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, wistfulThinking.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player2.getId())
                .doesNotContain(player1.getId());

        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
