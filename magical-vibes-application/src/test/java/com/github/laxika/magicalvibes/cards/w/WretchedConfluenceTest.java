package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WretchedConfluence.class, GiantSpider.class, GrizzlyBears.class, Spellbook.class})
class WretchedConfluenceTest extends BaseCardTest {

    @Test
    void playerModeDrawsAndLosesRepeatedly() {
        harness.setHand(player1, List.of(new WretchedConfluence()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addMana();

        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        cast(new int[]{0, 0, 1}, List.of(player1.getId(), player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void creatureModeGivesMinusTwoMinusTwo() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new WretchedConfluence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana();

        cast(new int[]{1, 0, 2}, List.of(creature.getId(), player1.getId(), graveyardCreature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void graveyardModeReturnsTargetCreatureToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WretchedConfluence()));
        addMana();

        cast(new int[]{2, 0, 0}, List.of(creature.getId(), player1.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void creatureModeRejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new WretchedConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 0, 0);
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, modes,
                null, null, List.of(artifact.getId(), player1.getId(), player2.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targetIds) {
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                null, null, targetIds, List.of());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 5);
    }
}
