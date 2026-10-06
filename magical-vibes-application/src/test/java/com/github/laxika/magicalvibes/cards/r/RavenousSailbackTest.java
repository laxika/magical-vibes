package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CorruptionOfTowashi;
import com.github.laxika.magicalvibes.cards.i.IchorDrinker;
import com.github.laxika.magicalvibes.cards.u.UrnOfGodfire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousSailback.class, UrnOfGodfire.class, CorruptionOfTowashi.class, IchorDrinker.class})
class RavenousSailbackTest extends BaseCardTest {

    @Test
    void hasteModeGrantsHasteUntilEndOfTurn() {
        Permanent sailback = cast(0, null);

        assertThat(gqs.hasKeyword(gd, sailback, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, sailback, Keyword.HASTE)).isFalse();
    }

    @Test
    void destroyModeDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());

        cast(1, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Urn of Godfire");
        harness.assertInGraveyard(player2, "Urn of Godfire");
    }

    @Test
    void destroyModeDestroysEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new CorruptionOfTowashi());

        cast(1, enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Corruption of Towashi");
        harness.assertInGraveyard(player2, "Corruption of Towashi");
    }

    @Test
    void destroyModeCannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IchorDrinker());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());
        castAndChooseMode(1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(artifact.getId()).doesNotContain(creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ichor Drinker");
    }

    @Test
    void destroyModeCanDestroyOwnArtifactAndDoesNotGrantHaste() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new UrnOfGodfire());

        Permanent sailback = cast(1, artifact.getId());

        harness.assertInGraveyard(player1, "Urn of Godfire");
        assertThat(gqs.hasKeyword(gd, sailback, Keyword.HASTE)).isFalse();
    }

    @Test
    void enteringWithoutBeingCastStillAllowsChoosingDestroyMode() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());

        Permanent sailback = harness.enterBattlefieldAndReturn(player1, new RavenousSailback());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Destroy target artifact or enchantment");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Urn of Godfire");
        assertThat(gqs.hasKeyword(gd, sailback, Keyword.HASTE)).isFalse();
    }

    private Permanent cast(int mode, UUID targetId) {
        castAndChooseMode(mode);
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RavenousSailback)
                .findFirst().orElseThrow();
    }

    private void castAndChooseMode(int mode) {
        harness.setHand(player1, List.of(new RavenousSailback()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, mode == 0
                ? "This creature gains haste until end of turn"
                : "Destroy target artifact or enchantment");
    }
}
