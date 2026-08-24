package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AbunaAcolyte;
import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralNet.class, AbunaAcolyte.class, BogImp.class, Forest.class, GrizzlyBears.class})
class CoralNetTest extends BaseCardTest {

    private Permanent attachTo(Permanent creature) {
        Permanent aura = new Permanent(new CoralNet());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Can enchant green and white creatures")
    void canEnchantGreenAndWhiteCreatures() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent whiteCreature = harness.addToBattlefieldAndReturn(player2, new AbunaAcolyte());

        harness.setHand(player1, List.of(new CoralNet(), new CoralNet()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, greenCreature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, whiteCreature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a creature that is neither green nor white")
    void cannotEnchantBlackCreature() {
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player2, new BogImp());

        harness.setHand(player1, List.of(new CoralNet()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, blackCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a green or white creature");
    }

    @Test
    @DisplayName("Enchanted creature's controller may discard at upkeep to keep it")
    void mayDiscardAtUpkeepToKeepEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachTo(creature);
        harness.setHand(player2, List.of(new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Enchanted creature is sacrificed when its controller declines to discard")
    void sacrificesEnchantedCreatureWhenDiscardIsDeclined() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachTo(creature);
        harness.setHand(player2, List.of(new Forest()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
