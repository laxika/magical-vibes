package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicBattleTest extends BaseCardTest {

    @Test
    void uniqueHighestManaValuePlayerMayChangeTheTarget() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var alternateTarget = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, originalTarget.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Psychic Battle"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, alternateTarget.getId());

        StackEntry spell = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Boomerang"))
                .findFirst()
                .orElseThrow();
        assertThat(spell.getTargetId()).isEqualTo(alternateTarget.getId());
    }

    @Test
    void tiedHighestManaValuesLeaveTargetsUnchanged() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        StackEntry spell = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Boomerang"))
                .findFirst()
                .orElseThrow();
        assertThat(spell.getTargetId()).isEqualTo(originalTarget.getId());
    }
}
