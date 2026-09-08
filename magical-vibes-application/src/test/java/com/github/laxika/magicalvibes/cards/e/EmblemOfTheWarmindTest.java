package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmblemOfTheWarmind.class, GrizzlyBears.class})
class EmblemOfTheWarmindTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste while Emblem of the Warmind is attached")
    void grantsHasteToCreaturesYouControl() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        attachEmblem(enchantedCreature);

        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is lost when Emblem of the Warmind leaves the battlefield")
    void losesHasteWhenEmblemLeaves() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent emblem = attachEmblem(creature);

        gd.playerBattlefields.get(player1.getId()).remove(emblem);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can only enchant a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmblemOfTheWarmind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private Permanent attachEmblem(Permanent creature) {
        Permanent emblem = new Permanent(new EmblemOfTheWarmind());
        emblem.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(emblem);
        return emblem;
    }
}
