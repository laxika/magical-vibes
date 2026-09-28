package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordsmansSteel.class, LeoninScimitar.class, GrizzlyBears.class, Forest.class})
class SwordsmansSteelTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws one card for each Equipment controlled")
    void entersAndDrawsForEachEquipmentControlled() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new SwordsmansSteel()));
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 for each controlled Equipment")
    void boostsEquippedCreatureForEachControlledEquipment() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent steel = harness.addToBattlefieldAndReturn(player1, new SwordsmansSteel());
        steel.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Equip ability attaches Swordsmans Steel to a creature you control")
    void equipsCreature() {
        Permanent steel = harness.addToBattlefieldAndReturn(player1, new SwordsmansSteel());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int steelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(steel);
        harness.activateAbility(player1, steelIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(steel.getAttachedTo()).isEqualTo(creature.getId());
    }
}
