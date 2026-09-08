package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScavengerRegentExudeToxin.class, HillGiant.class, ShivanDragon.class, Shock.class})
class ScavengerRegentExudeToxinTest extends BaseCardTest {

    @Test
    @DisplayName("Omen gives non-Dragons -X/-X and shuffles the card into its owner's library")
    void omenShrinksNonDragonsAndShuffles() {
        ScavengerRegentExudeToxin card = new ScavengerRegentExudeToxin();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        int ownPower = gqs.getEffectivePower(gd, ownCreature);
        int ownToughness = gqs.getEffectiveToughness(gd, ownCreature);
        int opposingPower = gqs.getEffectivePower(gd, opposingCreature);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingCreature);
        int dragonPower = gqs.getEffectivePower(gd, dragon);
        int dragonToughness = gqs.getEffectiveToughness(gd, dragon);

        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.playCardWithAlternateCost(gd, player1, 0, 2, null, null, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(ownPower - 2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(ownToughness - 2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(opposingPower - 2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(opposingToughness - 2);
        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(dragonPower);
        assertThat(gqs.getEffectiveToughness(gd, dragon)).isEqualTo(dragonToughness);
        assertThat(gd.playerDecks.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Ward triggers when an opponent targets Scavenger Regent")
    void wardTriggersOnOpponentSpell() {
        Permanent regent = new Permanent(new ScavengerRegentExudeToxin());
        regent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(regent);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, regent.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }
}
