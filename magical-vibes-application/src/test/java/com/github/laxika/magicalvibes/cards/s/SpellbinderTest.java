package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MetalFatigue;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Spellbinder.class, MetalFatigue.class, MyrMoonvessel.class})
class SpellbinderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can imprint an instant from hand")
    void etbImprintsInstantFromHand() {
        MetalFatigue metalFatigueCard = new MetalFatigue();
        harness.setHand(player1, List.of(new Spellbinder(), metalFatigueCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(metalFatigueCard);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(metalFatigueCard);

        Permanent spellbinder = findPermanent(player1, "Spellbinder");
        assertThat(gd.getImprintedCard(spellbinder.getCard())).isSameAs(metalFatigueCard);
    }

    @Test
    @DisplayName("Equipped creature dealing combat damage can cast a copy of the imprinted instant")
    void equippedCreatureCombatDamageCastsCopy() {
        Spellbinder spellbinderCard = new Spellbinder();
        MetalFatigue metalFatigueCard = new MetalFatigue();
        Permanent spellbinder = harness.addToBattlefieldAndReturn(player1, spellbinderCard);
        gd.setImprintedCard(spellbinderCard, metalFatigueCard);
        gd.exiledCards.add(new ExiledCardEntry(metalFatigueCard, player1.getId(), spellbinder.getId()));

        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        spellbinder.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.isCopy());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(metalFatigueCard);
    }

    @Test
    @DisplayName("Declining the ETB choice leaves the instant in hand")
    void decliningEtbLeavesInstantInHand() {
        MetalFatigue metalFatigueCard = new MetalFatigue();
        harness.setHand(player1, List.of(new Spellbinder(), metalFatigueCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Metal Fatigue");
        Permanent spellbinder = findPermanent(player1, "Spellbinder");
        assertThat(gd.getImprintedCard(spellbinder.getCard())).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(metalFatigueCard);
    }

    @Test
    @DisplayName("ETB does not offer a noninstant card for imprint")
    void etbDoesNotOfferNoninstantCard() {
        MyrMoonvessel myrMoonvesselCard = new MyrMoonvessel();
        harness.setHand(player1, List.of(new Spellbinder(), myrMoonvesselCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ImprintFromHandChoice.class)).isNull();
        harness.assertInHand(player1, "Myr Moonvessel");
        Permanent spellbinder = findPermanent(player1, "Spellbinder");
        assertThat(gd.getImprintedCard(spellbinder.getCard())).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(myrMoonvesselCard);
    }

    @Test
    @DisplayName("Equip {4} attaches Spellbinder to a creature")
    void equipAttachesToCreature() {
        Permanent spellbinder = harness.addToBattlefieldAndReturn(player1, new Spellbinder());
        spellbinder.setSummoningSick(false);
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spellbinder.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Declining the combat-damage copy leaves Spellbinder's imprint unchanged")
    void decliningCombatDamageCopyLeavesImprintUnchanged() {
        MetalFatigue metalFatigueCard = new MetalFatigue();
        Permanent spellbinder = addReadySpellbinderWithImprint(metalFatigueCard);
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        spellbinder.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(metalFatigueCard);
        assertThat(gd.getImprintedCard(spellbinder.getCard())).isSameAs(metalFatigueCard);
    }

    @Test
    @DisplayName("Declining the cast of the copied instant leaves the original exiled")
    void decliningCastLeavesOriginalExiled() {
        MetalFatigue metalFatigueCard = new MetalFatigue();
        Permanent spellbinder = addReadySpellbinderWithImprint(metalFatigueCard);
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        spellbinder.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(metalFatigueCard);
        assertThat(gd.getImprintedCard(spellbinder.getCard())).isSameAs(metalFatigueCard);
    }

    @Test
    @DisplayName("An unequipped creature dealing combat damage does not trigger Spellbinder")
    void unequippedCreatureDoesNotTrigger() {
        addReadySpellbinderWithImprint(new MetalFatigue());
        Permanent creature = addCreatureReady(player1, new MyrMoonvessel());
        creature.setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySpellbinderWithImprint(MetalFatigue imprintedCard) {
        Spellbinder spellbinderCard = new Spellbinder();
        Permanent spellbinder = harness.addToBattlefieldAndReturn(player1, spellbinderCard);
        spellbinder.setSummoningSick(false);
        gd.setImprintedCard(spellbinderCard, imprintedCard);
        gd.exiledCards.add(new ExiledCardEntry(imprintedCard, player1.getId(), spellbinder.getId()));
        return spellbinder;
    }
}
