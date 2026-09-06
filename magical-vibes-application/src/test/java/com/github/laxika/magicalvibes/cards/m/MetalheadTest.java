package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Metalhead.class, GrizzlyBears.class, Spellbook.class})
class MetalheadTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another artifact or creature to its owner's hand")
    void entersAndBouncesAnotherPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        castMetalhead();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInHand(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Metalhead");
    }

    @Test
    @DisplayName("ETB can return a creature")
    void entersAndBouncesCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMetalhead();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB has no trigger when no other artifact or creature exists")
    void entersWithoutAnotherArtifactOrCreature() {
        castMetalhead();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Metalhead");
    }

    @Test
    @DisplayName("Sacrificing another artifact puts a counter on Metalhead and grants menace and haste")
    void activationSacrificesArtifactAndGrantsKeywords() {
        Permanent metalhead = addReadyMetalhead();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(metalhead), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(metalhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(metalhead.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(metalhead.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(metalhead.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(metalhead.hasKeyword(Keyword.MENACE)).isFalse();
        assertThat(metalhead.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot sacrifice Metalhead itself or a nonartifact creature")
    void activationRequiresAnotherArtifact() {
        Permanent metalhead = addReadyMetalhead();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(metalhead), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another artifact");
    }

    private void castMetalhead() {
        harness.setHand(player1, List.of(new Metalhead()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private Permanent addReadyMetalhead() {
        Permanent metalhead = harness.addToBattlefieldAndReturn(player1, new Metalhead());
        metalhead.setSummoningSick(false);
        return metalhead;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
