package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NogiDracoZealot.class, DragonEgg.class, GrizzlyBears.class})
class NogiDracoZealotTest extends BaseCardTest {

    @Test
    @DisplayName("Dragon spells cost one less")
    void dragonSpellsCostOneLess() {
        harness.addToBattlefield(player1, new NogiDracoZealot());
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Non-Dragon creature spells are not reduced")
    void nonDragonCreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new NogiDracoZealot());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With three controlled Dragons, attacking makes Nogi a flying 5/5 Dragon")
    void threeControlledDragonsTransformNogiUntilEndOfTurn() {
        Permanent nogi = addReady(player1, new NogiDracoZealot());
        Permanent dragon1 = addReady(player1, new DragonEgg());
        Permanent dragon2 = addReady(player1, new DragonEgg());
        Permanent dragon3 = addReady(player1, new DragonEgg());

        assertThat(gqs.effectiveCreatureSubtypes(gd, dragon1)).contains(CardSubtype.DRAGON);
        assertThat(gqs.effectiveCreatureSubtypes(gd, dragon2)).contains(CardSubtype.DRAGON);
        assertThat(gqs.effectiveCreatureSubtypes(gd, dragon3)).contains(CardSubtype.DRAGON);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(nogi.getId());
        resolveAllTriggers();

        assertThat(nogi.getEffectivePower()).isEqualTo(5);
        assertThat(nogi.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, nogi, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, nogi)).containsExactly(CardSubtype.DRAGON);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(nogi.getEffectivePower()).isEqualTo(3);
        assertThat(nogi.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nogi, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, nogi)).doesNotContain(CardSubtype.DRAGON);
    }

    @Test
    @DisplayName("Opponent-controlled Dragons do not satisfy the attack condition")
    void opponentDragonsDoNotCount() {
        Permanent nogi = addReady(player1, new NogiDracoZealot());
        addReady(player1, new DragonEgg());
        addReady(player1, new DragonEgg());
        addReady(player2, new DragonEgg());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(nogi.getEffectivePower()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, nogi, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, nogi)).doesNotContain(CardSubtype.DRAGON);
    }

    private Permanent addReady(Player player, Card card) {
        return addCreatureReady(player, card);
    }
}
