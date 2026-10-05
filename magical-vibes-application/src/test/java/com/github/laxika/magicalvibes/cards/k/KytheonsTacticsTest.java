package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({KytheonsTactics.class, GrizzlyBears.class, Shock.class, LavaAxe.class})
class KytheonsTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +2/+1 until end of turn")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent creatures are unaffected")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(player1);

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Without spell mastery no vigilance is granted")
    void withoutSpellMasteryNoVigilance() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Spell mastery grants vigilance in addition to the boost")
    void spellMasteryGrantsVigilance() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Boost and vigilance wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Two instants satisfy spell mastery")
    void twoInstantsSatisfySpellMastery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        cast(player1);

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Two sorceries satisfy spell mastery without affecting opposing creatures")
    void twoSorceriesSatisfySpellMastery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LavaAxe(), new LavaAxe()));

        cast(player1);

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.VIGILANCE)).isTrue();
        Permanent opponent = findPermanent(player2, "Grizzly Bears");
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponent.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creature cards and opponent graveyards do not satisfy spell mastery")
    void onlyOwnInstantsAndSorceriesCount() {
        harness.setGraveyard(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock(), new LavaAxe()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(player1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Spell mastery is checked when the spell resolves")
    void spellMasteryChecksGraveyardAtResolution() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KytheonsTactics()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution receive neither bonus")
    void laterCreaturesAreUnaffected() {
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));

        cast(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    private void cast(Player player) {
        harness.setHand(player, List.of(new KytheonsTactics()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player, 0, 0);
    }
}
