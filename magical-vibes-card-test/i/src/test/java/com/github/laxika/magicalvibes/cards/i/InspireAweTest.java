package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AlseidOfLifesBounty;
import com.github.laxika.magicalvibes.cards.p.PheresBandBrawler;
import com.github.laxika.magicalvibes.cards.s.SentinelsEyes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspireAwe.class, PheresBandBrawler.class, SentinelsEyes.class, AlseidOfLifesBounty.class})
class InspireAweTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage except from enchanted and enchantment creatures")
    void preventsCombatDamageExceptFromExemptCreatures() {
        Permanent ordinaryCreature = addCreatureReady(player1, new PheresBandBrawler());
        Permanent enchantedCreature = addCreatureReady(player1, new PheresBandBrawler());
        attachAura(enchantedCreature);
        Permanent enchantmentCreature = addCreatureReady(player2, new AlseidOfLifesBounty());

        castInspireAwe();

        assertThat(gqs.isPreventedFromDealingDamage(gd, ordinaryCreature, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, enchantedCreature, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, enchantmentCreature, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, ordinaryCreature, false)).isFalse();
    }

    @Test
    @DisplayName("Checks whether a creature is enchanted immediately before combat damage")
    void checksEnchantmentStatusWhenDamageWouldBeDealt() {
        Permanent creature = addCreatureReady(player1, new PheresBandBrawler());
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

    @Test
    @DisplayName("Only exempt attackers deal combat damage to the defending player")
    void onlyExemptAttackersDealCombatDamage() {
        Permanent ordinary = addCreatureReady(player1, new PheresBandBrawler());
        Permanent enchanted = addCreatureReady(player1, new PheresBandBrawler());
        attachAura(enchanted);
        Permanent enchantment = addCreatureReady(player1, new AlseidOfLifesBounty());
        castInspireAwe();
        finishScry();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        for (Permanent attacker : List.of(ordinary, enchanted, enchantment)) {
            attacker.setAttacking(true);
        }
        harness.resolveCombatDamage();

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Applies to creatures entering later and auras controlled by an opponent")
    void appliesToLaterCreaturesAndOpponentAuras() {
        castInspireAwe();
        finishScry();
        Permanent creature = addCreatureReady(player2, new PheresBandBrawler());
        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isTrue();

        attachAura(creature);

        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isFalse();
    }

    @Test
    @DisplayName("Combat-damage prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new PheresBandBrawler());
        castInspireAwe();
        finishScry();
        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.isPreventedFromDealingDamage(gd, creature, true)).isFalse();
    }

    private void finishScry() {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    private void castInspireAwe() {
        harness.setHand(player1, List.of(new InspireAwe()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SentinelsEyes());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
