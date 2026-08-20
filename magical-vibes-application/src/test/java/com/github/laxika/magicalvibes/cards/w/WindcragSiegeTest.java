package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HungrySpriggan;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WindcragSiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Jeskai creates a hasty lifelinking Goblin at upkeep")
    void jeskaiCreatesGoblinAtUpkeep() {
        castAndChoose("Jeskai");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> goblins = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(goblins).hasSize(1);
        Permanent goblin = goblins.getFirst();
        assertThat(goblin.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblin.getCard().getSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(goblin.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(goblin.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Mardu makes direct attack triggers trigger twice")
    void marduDoublesDirectAttackTrigger() {
        Permanent spriggan = addCreatureReady(player1, new HungrySpriggan());
        castAndChoose("Mardu");

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(spriggan.getPowerModifier()).isEqualTo(6);
        assertThat(spriggan.getToughnessModifier()).isEqualTo(6);
    }

    private void castAndChoose(String mode) {
        harness.setHand(player1, List.of(new WindcragSiege()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("Mardu", "Jeskai");
        harness.handleListChoice(player1, mode);
    }
}
