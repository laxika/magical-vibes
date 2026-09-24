package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvyElemental;
import com.github.laxika.magicalvibes.cards.m.MindSpring;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OwlinSpiralmancer.class, GrizzlyBears.class, IvyElemental.class, MindSpring.class})
class OwlinSpiralmancerTest extends BaseCardTest {

    @Test
    @DisplayName("May copy the first X spell and preserves its X value")
    void copiesFirstXSpell() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 1);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A copied permanent X spell becomes a token")
    void copiedPermanentSpellBecomesToken() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0, 2);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Ivy Elemental"))
                .hasSize(2)
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Only the first X spell each turn can be copied")
    void onlyCopiesFirstXSpellEachTurn() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new MindSpring(), new MindSpring()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.castSorcery(player1, 0, 1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A non-X spell does not trigger Owlin Spiralmancer")
    void nonXSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new OwlinSpiralmancer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
