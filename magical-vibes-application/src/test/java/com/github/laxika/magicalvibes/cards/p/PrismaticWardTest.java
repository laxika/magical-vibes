package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrismaticWardTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createDamageInstant(String name, CardColor color, String manaCost, int amount) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(amount));
        return card;
    }

    /** Adds a warded creature (aura attached, given chosen colour) to the player's battlefield. */
    private Permanent addWardedCreature(Player owner, int power, int toughness, CardColor sourceColor, CardColor chosen) {
        Permanent creature = new Permanent(createCreature("Warded One", power, toughness, sourceColor));
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(creature);

        Permanent ward = new Permanent(new PrismaticWard());
        ward.setAttachedTo(creature.getId());
        ward.setChosenColor(chosen);
        gd.playerBattlefields.get(owner.getId()).add(ward);
        return creature;
    }

    // ===== Casting: choose a color as it enters =====

    @Test
    @DisplayName("Resolving Prismatic Ward attaches to a creature and awaits a color choice")
    void resolvingAwaitsColorChoice() {
        Permanent target = new Permanent(createCreature("Grizzly", 2, 2, CardColor.GREEN));
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.setHand(player1, List.of(new PrismaticWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on the Aura permanent")
    void choosingColorSetsOnAura() {
        Permanent target = new Permanent(createCreature("Grizzly", 2, 2, CardColor.GREEN));
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.setHand(player1, List.of(new PrismaticWard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent ward = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Prismatic Ward"))
                .findFirst().orElseThrow();
        assertThat(ward.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(ward.getAttachedTo()).isEqualTo(target.getId());
    }

    // ===== Noncombat damage =====

    @Test
    @DisplayName("Prevents noncombat damage from a source of the chosen color")
    void preventsChosenColorNoncombatDamage() {
        Permanent warded = addWardedCreature(player2, 2, 2, CardColor.GREEN, CardColor.RED);

        harness.setHand(player1, List.of(createDamageInstant("Red Bolt", CardColor.RED, "{R}", 2)));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, warded.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(warded.getId()));
        assertThat(warded.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows noncombat damage from a source of a different color")
    void allowsOtherColorNoncombatDamage() {
        Permanent warded = addWardedCreature(player2, 2, 2, CardColor.GREEN, CardColor.RED);

        harness.setHand(player1, List.of(createDamageInstant("Blue Blast", CardColor.BLUE, "{U}", 2)));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, warded.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(warded.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Warded One"));
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Prevents combat damage from a creature of the chosen color")
    void preventsChosenColorCombatDamage() {
        Permanent warded = addWardedCreature(player2, 2, 2, CardColor.GREEN, CardColor.RED);
        warded.setBlocking(true);
        warded.addBlockingTarget(0);

        Permanent attacker = new Permanent(createCreature("Fire Elemental", 5, 4, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(warded.getId()));
        assertThat(warded.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows combat damage from a creature of a different color")
    void allowsOtherColorCombatDamage() {
        Permanent warded = addWardedCreature(player2, 2, 2, CardColor.GREEN, CardColor.RED);
        warded.setBlocking(true);
        warded.addBlockingTarget(0);

        Permanent attacker = new Permanent(createCreature("Big Green", 3, 3, CardColor.GREEN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(warded.getId()));
    }
}
