package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.FolkOfThePines;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.s.SoldeviGolem;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrismaticWard.class, BalduvianBears.class, Incinerate.class, ZuranSpellcaster.class,
        BalduvianBarbarians.class, FolkOfThePines.class, SoldeviGolem.class, IcyManipulator.class})
class PrismaticWardTest extends BaseCardTest {

    private static Card createMulticoloredDamageInstant() {
        Card card = new Card();
        card.setName("Test Multicolored Damage Spell");
        card.setType(CardType.INSTANT);
        card.setManaCost("{U}{R}");
        card.setColors(List.of(CardColor.BLUE, CardColor.RED));
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
        return card;
    }

    /** Adds a warded creature (aura attached, given chosen colour) to the player's battlefield. */
    private Permanent addWardedCreature(Player owner, CardColor chosen) {
        Permanent creature = addCreatureReady(owner, new BalduvianBears());

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
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new PrismaticWard()));
        addPrismaticWardMana();

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color sets chosenColor on the Aura permanent")
    void choosingColorSetsOnAura() {
        Permanent target = addCreatureReady(player1, new BalduvianBears());

        harness.setHand(player1, List.of(new PrismaticWard()));
        addPrismaticWardMana();

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent ward = findPermanent(player1, "Prismatic Ward");
        assertThat(ward.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(ward.getAttachedTo()).isEqualTo(target.getId());
    }

    // ===== Noncombat damage =====

    @Test
    @DisplayName("Prevents noncombat damage from a source of the chosen color")
    void preventsChosenColorNoncombatDamage() {
        Permanent warded = addWardedCreature(player2, CardColor.RED);

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, warded.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(warded.getId()));
        assertThat(warded.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents noncombat damage from a multicolored source that includes the chosen color")
    void preventsChosenColorDamageFromMulticoloredSource() {
        Permanent warded = addWardedCreature(player2, CardColor.RED);

        harness.setHand(player1, List.of(createMulticoloredDamageInstant()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, warded.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(warded.getId()));
        assertThat(warded.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows noncombat damage from a source of a different color")
    void allowsOtherColorNoncombatDamage() {
        Permanent warded = addWardedCreature(player2, CardColor.RED);
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(spellcaster), null,
                warded.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(warded.getId()));
        assertThat(warded.getMarkedDamage()).isEqualTo(1);
    }

    // ===== Combat damage =====

    @Test
    @DisplayName("Prevents combat damage from a creature of the chosen color")
    void preventsChosenColorCombatDamage() {
        Permanent warded = addWardedCreature(player2, CardColor.RED);
        Permanent attacker = addCreatureReady(player1, new BalduvianBarbarians());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(warded),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(warded.getId()));
        assertThat(warded.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Allows combat damage from a creature of a different color")
    void allowsOtherColorCombatDamage() {
        Permanent warded = addWardedCreature(player2, CardColor.RED);
        Permanent attacker = addCreatureReady(player1, new FolkOfThePines());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(warded),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(warded.getId()));
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Does not prevent damage from a colorless source")
    void doesNotPreventColorlessSourceDamage() {
        Permanent warded = addWardedCreature(player2, CardColor.RED);
        Permanent attacker = addCreatureReady(player1, new SoldeviGolem());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(warded),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(warded.getId()));
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new PrismaticWard()));
        addPrismaticWardMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addPrismaticWardMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
