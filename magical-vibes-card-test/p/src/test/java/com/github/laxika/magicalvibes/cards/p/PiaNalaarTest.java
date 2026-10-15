package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({PiaNalaar.class, Ornithopter.class, GrizzlyBears.class, Spellbook.class})
class PiaNalaarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Thopter artifact creature token with flying")
    void etbCreatesThopterToken() {
        harness.setHand(player1, List.of(new PiaNalaar()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("First ability gives a target artifact creature +1/+0 until end of turn")
    void boostsTargetArtifactCreature() {
        addCreatureReady(player1, new PiaNalaar());
        Permanent thopter = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, thopter.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("First ability cannot target a nonartifact creature")
    void cannotBoostNonartifactCreature() {
        addCreatureReady(player1, new PiaNalaar());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    @DisplayName("Second ability sacrifices an artifact and makes a target creature unable to block this turn")
    void sacrificesArtifactAndPreventsBlocking() {
        addCreatureReady(player1, new PiaNalaar());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Pump can target an opposing artifact creature repeatedly and expires at cleanup")
    void repeatedPumpOfOpposingCreatureExpires() {
        harness.addToBattlefield(player1, new PiaNalaar());
        Permanent thopter = addCreatureReady(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, thopter.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, thopter.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, thopter)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Pump cannot target an artifact that is not a creature")
    void cannotBoostNoncreatureArtifact() {
        addCreatureReady(player1, new PiaNalaar());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An artifact creature is sacrificed as a cost before the blocking restriction resolves")
    void artifactCreatureSacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new PiaNalaar());
        addCreatureReady(player1, new Ornithopter());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        assertThat(bears.isCantBlockThisTurn()).isFalse();

        harness.passBothPriorities();
        assertThat(bears.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(bears.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotActivateWithoutAnArtifactYouControl() {
        addCreatureReady(player1, new PiaNalaar());
        harness.addToBattlefield(player2, new Spellbook());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(bears.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("The artifact creature targeted by the second ability can also pay its sacrifice cost")
    void canSacrificeTheTargetedArtifactCreature() {
        harness.addToBattlefield(player1, new PiaNalaar());
        Permanent thopter = addCreatureReady(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, thopter.getId());
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(thopter.isCantBlockThisTurn()).isFalse();
        harness.assertOnBattlefield(player1, "Pia Nalaar");
    }
}
