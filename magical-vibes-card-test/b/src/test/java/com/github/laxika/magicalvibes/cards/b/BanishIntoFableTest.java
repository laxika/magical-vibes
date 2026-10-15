package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishIntoFable.class, GrizzlyBears.class, HistoryOfBenalia.class,
        Island.class, Ornithopter.class})
class BanishIntoFableTest extends BaseCardTest {

    @Test
    void returnsTargetAndCreatesKnight() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Knight")).hasSize(1);
    }

    @Test
    void copiesOnceForArtifactAndTwiceForArtifactAndEnchantment() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstCopyTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCopyTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        prepareSpell();

        harness.castInstant(player1, 0, originalTarget.getId());
        resolveCopyTo(firstCopyTarget);
        resolveCopyTo(secondCopyTarget);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Knight")).hasSize(3);
        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(3);
    }

    @Test
    void checksCopyConditionsWhenTheTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        prepareSpell();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(1);
    }

    @Test
    void onlyTargetsNonlandPermanents() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BanishIntoFable()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void resolveCopyTo(Permanent target) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
