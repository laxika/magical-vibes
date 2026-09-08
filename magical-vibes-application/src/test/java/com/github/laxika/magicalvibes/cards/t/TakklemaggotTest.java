package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TakklemaggotTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -0/-1 counter on the enchanted creature during its controller's upkeep")
    void upkeepPutsMinusZeroMinusOneCounterOnEnchantedCreature() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        castTakklemaggotOn(player1, enchantedCreature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(enchantedCreature.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(enchantedCreature.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Returns attached to the only creature it could enchant when the enchanted creature dies")
    void returnsAttachedToOnlyLegalCreature() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent replacementCreature = addCreatureReady(player1, new GrizzlyBears());
        castTakklemaggotOn(player1, enchantedCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantedCreature));
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Takklemaggot");
        assertThat(aura.getAttachedTo()).isEqualTo(replacementCreature.getId());
        harness.assertNotInGraveyard(player1, "Takklemaggot");
    }

    @Test
    @DisplayName("The enchanted creature's controller chooses among legal creatures")
    void dyingCreatureControllerChoosesLegalCreature() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent firstChoice = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castTakklemaggotOn(player1, enchantedCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantedCreature));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, firstChoice.getId());

        Permanent aura = findPermanent(player1, "Takklemaggot");
        assertThat(aura.getAttachedTo()).isEqualTo(firstChoice.getId());
    }

    @Test
    @DisplayName("Returns as a non-Aura and damages the dying creature's controller when no legal creature exists")
    void returnsAsNonAuraWhenNoLegalCreatureExists() {
        Permanent enchantedCreature = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new WhiteKnight());
        castTakklemaggotOn(player1, enchantedCreature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantedCreature));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Takklemaggot");
        assertThat(returned.getAttachedTo()).isNull();
        assertThat(returned.getCard().isAura()).isFalse();

        int player1Life = gd.playerLifeTotals.get(player1.getId());
        int player2Life = gd.playerLifeTotals.get(player2.getId());
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(player2Life - 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(player1Life);
    }

    private void castTakklemaggotOn(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Takklemaggot()));
        harness.addMana(caster, ManaColor.BLACK, 4);
        harness.castEnchantment(caster, 0, target.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(caster, "Takklemaggot").getAttachedTo()).isEqualTo(target.getId());
    }
}
