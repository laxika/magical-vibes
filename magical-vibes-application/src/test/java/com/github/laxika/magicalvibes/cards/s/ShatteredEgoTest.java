package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShatteredEgo.class, GrizzlyBears.class})
class ShatteredEgoTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -3/-0")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = addCreatureReady(player2);
        attachAura(player1, creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability puts the enchanted creature third from the top of its owner's library")
    void abilityPutsEnchantedCreatureThirdFromTop() {
        Permanent creature = addCreatureReady(player2);
        Permanent aura = attachAura(player1, creature);
        List<Card> library = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, library);

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aura), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).get(2)).isSameAs(creature.getOriginalCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent attachAura(com.github.laxika.magicalvibes.model.Player controller, Permanent creature) {
        Permanent aura = new Permanent(new ShatteredEgo());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
