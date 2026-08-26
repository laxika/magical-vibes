package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealityAcid.class, Disenchant.class, Spellbook.class})
class RealityAcidTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three time counters")
    void entersWithTimeCounters() {
        Permanent spellbook = addSpellbook(player2);

        harness.setHand(player1, List.of(new RealityAcid()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0, spellbook.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Reality Acid");
        assertThat(aura.getCounterCount(CounterType.TIME)).isEqualTo(3);
        assertThat(aura.getAttachedTo()).isEqualTo(spellbook.getId());
    }

    @Test
    @DisplayName("Removes one time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent spellbook = addSpellbook(player2);
        Permanent aura = addAuraAttachedTo(player1, spellbook);
        aura.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(aura.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent spellbook = addSpellbook(player2);
        Permanent aura = addAuraAttachedTo(player1, spellbook);
        aura.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Reality Acid");
        harness.assertInGraveyard(player1, "Reality Acid");
    }

    @Test
    @DisplayName("When it leaves the battlefield, the enchanted permanent is sacrificed")
    void sacrificesEnchantedPermanentWhenAuraLeaves() {
        Permanent spellbook = addSpellbook(player2);
        Permanent aura = addAuraAttachedTo(player1, spellbook);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    private Permanent addSpellbook(com.github.laxika.magicalvibes.model.Player controller) {
        Permanent spellbook = new Permanent(new Spellbook());
        gd.playerBattlefields.get(controller.getId()).add(spellbook);
        return spellbook;
    }

    private Permanent addAuraAttachedTo(com.github.laxika.magicalvibes.model.Player controller,
            Permanent enchanted) {
        Permanent aura = new Permanent(new RealityAcid());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
