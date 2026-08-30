package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlseidOfLifesBounty;
import com.github.laxika.magicalvibes.cards.a.AqueousForm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspireAwe.class, GrizzlyBears.class, AqueousForm.class, AlseidOfLifesBounty.class})
class InspireAweTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage except from enchanted and enchantment creatures")
    void preventsCombatDamageExceptFromExemptCreatures() {
        Permanent ordinaryCreature = addCreature(player1, new GrizzlyBears());
        Permanent enchantedCreature = addCreature(player1, new GrizzlyBears());
        attachAura(enchantedCreature);
        Permanent enchantmentCreature = addCreature(player2, new AlseidOfLifesBounty());

        castInspireAwe();

        assertThat(gqs.isPreventedFromDealingDamage(gd, ordinaryCreature, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, enchantedCreature, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, enchantmentCreature, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, ordinaryCreature, false)).isFalse();
    }

    @Test
    @DisplayName("Checks whether a creature is enchanted immediately before combat damage")
    void checksEnchantmentStatusWhenDamageWouldBeDealt() {
        Permanent creature = addCreature(player1, new GrizzlyBears());
        castInspireAwe();

        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isTrue();

        Permanent aura = attachAura(creature);
        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isTrue();
    }

    @Test
    @DisplayName("Scry 2 resolves after the combat-damage prevention is set")
    void scriesTwo() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card first = deck.get(0);
        Card second = deck.get(1);

        castInspireAwe();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(deck.get(0)).isSameAs(second);
        assertThat(deck.get(1)).isSameAs(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Inspire Awe");
    }

    private void castInspireAwe() {
        harness.setHand(player1, List.of(new InspireAwe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new AqueousForm());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
