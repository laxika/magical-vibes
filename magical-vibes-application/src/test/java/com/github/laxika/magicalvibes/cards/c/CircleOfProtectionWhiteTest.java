package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcatianMoneychanger;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import java.util.UUID;
import com.github.laxika.magicalvibes.cards.f.FieryJustice;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfProtectionWhite.class, KjeldoranWarrior.class, ZuranSpellcaster.class, FieryJustice.class, GrizzlyBears.class, IcatianMoneychanger.class, WhiteKnight.class})
class CircleOfProtectionWhiteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts for a white source choice")
    void resolvingAbilityPromptsForWhiteSource() {
        addReadyCircle(player1);
        Permanent source = addCreatureReady(player2, new KjeldoranWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(source.getId());
    }

    @Test
    @DisplayName("Choosing a white source records a one-shot prevention shield")
    void choosingWhiteSourceRecordsShield() {
        addReadyCircle(player1);
        Permanent soldier = addCreatureReady(player2, new KjeldoranWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId()) && s.sourceId().equals(soldier.getId()));
    }

    @Test
    @DisplayName("Prevents the next combat damage from the chosen source and consumes the shield")
    void preventsNextCombatDamageAndConsumesShield() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent soldier = addCreatureReady(player2, new KjeldoranWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, soldier.getId());

        soldier.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Only the chosen source is prevented; a different white source still deals damage")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent chosen = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent other = addCreatureReady(player2, new KjeldoranWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        // The unchosen 1/1 deals its damage; the shield is untouched
        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("A non-white source is not a valid choice; the chosen white source is unaffected")
    void nonWhiteSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        Permanent white = addCreatureReady(player2, new KjeldoranWarrior());
        Permanent blue = addCreatureReady(player2, new ZuranSpellcaster());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(white.getId()).doesNotContain(blue.getId());
        harness.handlePermanentChosen(player1, white.getId());

        // The blue creature isn't the chosen (white) source, so its damage is not prevented.
        blue.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(white.getId()));
    }

    @Test
    @DisplayName("A white spell on the stack is a legal source choice")
    void whiteSpellOnStackIsLegalSourceChoice() {
        addReadyCircle(player1);
        FieryJustice fieryJustice = new FieryJustice();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(fieryJustice));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player2, 0, player1.getId(), Map.of(player1.getId(), 5));
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(fieryJustice.getId());
        harness.handlePermanentChosen(player1, fieryJustice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyCircle(player1);
        Permanent soldier = addCreatureReady(player2, new KjeldoranWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, soldier.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyCircle(Player player) {
        return addCreatureReady(player, new CircleOfProtectionWhite());
    }

    @Test
    void preventsDamageFromPermanentWhiteSpellAfterItResolves() {
        harness.setLife(player1, 20);
        addReadyCircle(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        IcatianMoneychanger moneychangerSpell = new IcatianMoneychanger();
        harness.castFromHand(player1, moneychangerSpell, "{W}");
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(moneychangerSpell.getId());
        harness.handlePermanentChosen(player1, moneychangerSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private Permanent addReadyWhiteCreature(Player player) {
        return addCreatureReady(player, new WhiteKnight());
    }

    private Permanent addReadyNonWhiteCreature(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
