package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongshotRebelBowman.class, Divination.class, GrizzlyBears.class, Opt.class})
class LongshotRebelBowmanTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spells you cast cost {1} less")
    void noncreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new LongshotRebelBowman());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("Creature spells are not reduced")
    void creatureSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new LongshotRebelBowman());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a noncreature spell triggers 2 damage to each opponent")
    void noncreatureSpellTriggersDamage() {
        harness.addToBattlefield(player1, new LongshotRebelBowman());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Longshot, Rebel Bowman"));

        harness.passBothPriorities();

        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger damage")
    void creatureSpellDoesNotTriggerDamage() {
        harness.addToBattlefield(player1, new LongshotRebelBowman());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
