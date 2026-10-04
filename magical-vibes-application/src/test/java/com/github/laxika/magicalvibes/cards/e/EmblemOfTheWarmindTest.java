package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
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

@CardUsed({EmblemOfTheWarmind.class, BlindPhantasm.class})
class EmblemOfTheWarmindTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have haste while Emblem of the Warmind is attached")
    void grantsHasteToCreaturesYouControl() {
        Permanent enchantedCreature = addCreatureReady(player1, new BlindPhantasm());
        Permanent otherCreature = addCreatureReady(player1, new BlindPhantasm());
        Permanent opponentCreature = addCreatureReady(player2, new BlindPhantasm());
        attachEmblem(enchantedCreature);

        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Haste is lost when Emblem of the Warmind leaves the battlefield")
    void losesHasteWhenEmblemLeaves() {
        Permanent creature = addCreatureReady(player1, new BlindPhantasm());
        Permanent emblem = attachEmblem(creature);

        gd.playerBattlefields.get(player1.getId()).remove(emblem);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Can only enchant a creature you control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new BlindPhantasm());
        harness.setHand(player1, List.of(new EmblemOfTheWarmind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Resolves attached to its target and grants haste to creatures entering later")
    void resolvesAndGrantsHasteToLaterCreatures() {
        Permanent enchantedCreature = addCreatureReady(player1, new BlindPhantasm());
        harness.setHand(player1, List.of(new EmblemOfTheWarmind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        harness.passBothPriorities();

        Permanent emblem = findPermanent(player1, "Emblem of the Warmind");
        assertThat(emblem.getAttachedTo()).isEqualTo(enchantedCreature.getId());
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.HASTE)).isTrue();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new BlindPhantasm());
        Permanent opponentCreature = harness.enterBattlefieldAndReturn(player2, new BlindPhantasm());
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Aura goes to the graveyard when its enchanted creature changes controller")
    void losesHasteWhenEnchantedCreatureChangesController() {
        Permanent enchantedCreature = addCreatureReady(player1, new BlindPhantasm());
        Permanent otherCreature = addCreatureReady(player1, new BlindPhantasm());
        attachEmblem(enchantedCreature);

        gd.playerBattlefields.get(player1.getId()).remove(enchantedCreature);
        gd.playerBattlefields.get(player2.getId()).add(enchantedCreature);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Emblem of the Warmind");
        harness.assertInGraveyard(player1, "Emblem of the Warmind");
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, enchantedCreature, Keyword.HASTE)).isFalse();
    }

    private Permanent attachEmblem(Permanent creature) {
        Permanent emblem = harness.addToBattlefieldAndReturn(player1, new EmblemOfTheWarmind());
        emblem.setAttachedTo(creature.getId());
        return emblem;
    }
}
