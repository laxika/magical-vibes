package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimBackwoodsTest extends BaseCardTest {

    

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tappingForManaAddsColorless() {
        Permanent backwoods = addReadyBackwoods(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(backwoods.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draw ability sacrifices chosen creature and draws a card")
    void drawAbilitySacrificesChosenCreatureAndDraws() {
        Permanent backwoods = addReadyBackwoods(player1);
        Permanent bear = addCreature(player1, "Chosen Bear");
        addCreature(player1, "Other Bear");
        addDrawAbilityMana(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, bear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chosen Bear"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chosen Bear"));
        assertThat(backwoods.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getEffectsToResolve()).containsExactly(new DrawCardEffect(1));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Draw ability auto-sacrifices the only eligible creature")
    void drawAbilityAutoSacrificesOnlyCreature() {
        addReadyBackwoods(player1);
        addCreature(player1, "Only Creature");
        addDrawAbilityMana(player1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Only Creature"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Only Creature"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate draw ability without enough mana")
    void cannotActivateDrawAbilityWithoutEnoughMana() {
        addReadyBackwoods(player1);
        addCreature(player1, "Creature");
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate draw ability without a creature to sacrifice")
    void cannotActivateDrawAbilityWithoutCreature() {
        addReadyBackwoods(player1);
        addDrawAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must choose a creature to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate draw ability when Grim Backwoods is tapped")
    void cannotActivateDrawAbilityWhenTapped() {
        Permanent backwoods = addReadyBackwoods(player1);
        backwoods.tap();
        addCreature(player1, "Creature");
        addDrawAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Non-creature permanents are not eligible for sacrifice")
    void nonCreaturePermanentsAreNotEligibleForSacrifice() {
        addReadyBackwoods(player1);
        addNonCreaturePermanent(player1);
        addCreature(player1, "Only Creature");
        addDrawAbilityMana(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Test Enchantment"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Only Creature"));
    }

    private Permanent addReadyBackwoods(Player player) {
        Permanent permanent = new Permanent(new GrimBackwoods());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreature(Player player, String name) {
        Permanent permanent = new Permanent(createCreature(name));
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addNonCreaturePermanent(Player player) {
        Card enchantment = new Card();
        enchantment.setName("Test Enchantment");
        enchantment.setType(CardType.ENCHANTMENT);
        enchantment.setManaCost("{1}{G}");
        enchantment.setColor(CardColor.GREEN);
        gd.playerBattlefields.get(player.getId()).add(new Permanent(enchantment));
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private void addDrawAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
