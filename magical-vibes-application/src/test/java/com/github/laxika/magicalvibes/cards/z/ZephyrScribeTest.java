package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZephyrScribe.class, Forest.class, Shock.class})
class ZephyrScribeTest extends BaseCardTest {

    @Test
    void activatingTheAbilityDrawsThenDiscards() {
        Permanent scribe = addReadyScribe();
        harness.setHand(player1, List.of(new Shock()));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Forest.class);
        assertThat(scribe.isTapped()).isTrue();
    }

    @Test
    void castingANoncreatureSpellUntapsZephyrScribe() {
        Permanent scribe = addTappedScribe();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveStack();

        assertThat(scribe.isTapped()).isFalse();
    }

    @Test
    void castingACreatureSpellDoesNotUntapZephyrScribe() {
        Permanent scribe = addTappedScribe();
        harness.setHand(player1, List.of(new ZephyrScribe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveStack();

        assertThat(scribe.isTapped()).isTrue();
    }

    private Permanent addReadyScribe() {
        Permanent scribe = harness.addToBattlefieldAndReturn(player1, new ZephyrScribe());
        scribe.setSummoningSick(false);
        return scribe;
    }

    private Permanent addTappedScribe() {
        Permanent scribe = addReadyScribe();
        scribe.tap();
        return scribe;
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
