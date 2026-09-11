package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisionsOfBrutality.class, GrizzlyBears.class, ZuranSpellcaster.class})
class VisionsOfBrutalityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new VisionsOfBrutality());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature's controller loses life equal to damage dealt")
    void controllerLosesLifeEqualToDamageDealt() {
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());

        harness.setHand(player1, List.of(new VisionsOfBrutality()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, spellcaster.getId());
        harness.passBothPriorities();

        int playerOneLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int playerTwoLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(playerOneLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(playerTwoLifeBefore - 1);
    }
}
