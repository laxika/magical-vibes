package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlackPoplarShaman;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThorntoothWitchTest extends BaseCardTest {

    /** Casts Black Poplar Shaman (a Treefolk spell) from player1's hand. */
    private void castTreefolkSpell() {
        harness.setHand(player1, List.of(new BlackPoplarShaman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    @Test
    @DisplayName("Casting a Treefolk spell triggers the may ability")
    void treefolkSpellTriggers() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castTreefolkSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting gives target creature +3/-3 until end of turn")
    void acceptBuffsAndDebuffsTarget() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GiantSpider());
        UUID spiderId = harness.getPermanentId(player2, "Giant Spider");

        castTreefolkSpell();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, spiderId);
        harness.passBothPriorities();

        Permanent spider = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(spider.getPowerModifier()).isEqualTo(3);
        assertThat(spider.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("+3/-3 kills a 2/2 creature")
    void debuffKillsSmallCreature() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        castTreefolkSpell();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the target unchanged")
    void declineLeavesTarget() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GiantSpider());

        castTreefolkSpell();
        harness.handleMayAbilityChosen(player1, false);

        Permanent spider = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(spider.getPowerModifier()).isEqualTo(0);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a non-Treefolk spell does not trigger")
    void nonTreefolkSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
