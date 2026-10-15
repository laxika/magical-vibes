package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Banefire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LodestoneGolem;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.r.RuptureSpire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaelstromArchangel.class, GrizzlyBears.class, Banefire.class,
        RuptureSpire.class, RuleOfLaw.class, LodestoneGolem.class})
class MaelstromArchangelTest extends BaseCardTest {

    @Test
    @DisplayName("On combat damage to a player, may cast a hand spell for free (no mana paid)")
    void castsHandSpellForFree() {
        addAttackingArchangel(player1);
        GrizzlyBears freeSpell = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(freeSpell)));
        // Deliberately add no mana: the spell must be castable without paying its cost.

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(freeSpell.getId());
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the card in hand and casts nothing")
    void decliningCastsNothing() {
        addAttackingArchangel(player1);
        GrizzlyBears handCard = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(handCard)));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No offer when the hand is empty")
    void noOfferWithEmptyHand() {
        addAttackingArchangel(player1);
        harness.setHand(player1, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("No trigger when blocked and no combat damage reaches a player")
    void noTriggerWhenBlocked() {
        addAttackingArchangel(player1);
        Permanent blocker = addCreatureReady(player2, new MaelstromArchangel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lands in hand cannot be cast with the combat damage trigger")
    void doesNotOfferLand() {
        addAttackingArchangel(player1);
        harness.setHand(player1, List.of(new RuptureSpire()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Rupture Spire");
    }

    @Test
    @DisplayName("May decline one hand card and cast another, but only one spell per trigger")
    void choosesOnlyOneSpell() {
        addAttackingArchangel(player1);
        MaelstromArchangel first = new MaelstromArchangel();
        MaelstromArchangel second = new MaelstromArchangel();
        MaelstromArchangel third = new MaelstromArchangel();
        harness.setHand(player1, List.of(first, second, third));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(second.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(c -> c.getId())
                .containsExactly(first.getId(), third.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("A sorcery with X in its mana cost is cast during combat with X equal to zero")
    void castsSorceryWithZeroX() {
        addAttackingArchangel(player1);
        Banefire spell = new Banefire();
        harness.setHand(player1, List.of(spell));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player1, "Banefire");
    }

    @Test
    @DisplayName("Rule of Law still prevents casting a second spell through the trigger")
    void respectsSpellLimit() {
        addAttackingArchangel(player1);
        harness.addToBattlefield(player2, new RuleOfLaw());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new MaelstromArchangel()));

        resolveCombatAndTrigger();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInHand(player1, "Maelstrom Archangel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without mana, Lodestone Golem's tax prevents the free cast")
    void cannotIgnoreUnpayableManaTax() {
        addAttackingArchangel(player1);
        harness.addToBattlefield(player2, new LodestoneGolem());
        harness.setHand(player1, List.of(new MaelstromArchangel()));

        resolveCombatAndTrigger();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertInHand(player1, "Maelstrom Archangel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting without paying the mana cost still pays Lodestone Golem's tax")
    void paysManaTax() {
        addAttackingArchangel(player1);
        harness.addToBattlefield(player2, new LodestoneGolem());
        MaelstromArchangel spell = new MaelstromArchangel();
        harness.setHand(player1, List.of(spell));

        resolveCombatAndTrigger();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.getSpellCastManaSpent(spell.getId())).isEqualTo(1);
    }

    private Permanent addAttackingArchangel(Player player) {
        Permanent archangel = addCreatureReady(player, new MaelstromArchangel());
        archangel.setAttacking(true);
        return archangel;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
