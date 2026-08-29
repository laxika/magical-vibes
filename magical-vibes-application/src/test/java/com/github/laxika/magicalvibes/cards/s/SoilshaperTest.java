package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoilshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell animates target land into a 3/3 that is still a land")
    void arcaneSpellAnimatesLand() {
        harness.addToBattlefield(player1, new Soilshaper());
        Permanent forest = addForest(player1);

        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, (java.util.UUID) null);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Casting a Spirit spell animates target land")
    void spiritSpellAnimatesLand() {
        harness.addToBattlefield(player1, new Soilshaper());
        Permanent forest = addForest(player1);

        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        harness.addToBattlefield(player1, new Soilshaper());
        Permanent forest = addForest(player1);

        harness.setHand(player1, List.of(new DesperateRitual()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, (java.util.UUID) null);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        forest.resetModifiers();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Soilshaper());
        addForest(player1);

        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addForest(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
