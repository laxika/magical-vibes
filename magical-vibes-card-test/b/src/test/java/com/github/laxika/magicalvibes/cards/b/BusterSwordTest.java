package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BusterSword.class, CounselOfTheSoratami.class, CrawWurm.class, Forest.class,
        GrizzlyBears.class})
class BusterSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Combat damage draws first, then offers a spell with mana value at most the damage")
    void drawsThenOffersEligibleSpell() {
        Permanent creature = addAttacker(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        CrawWurm tooExpensive = new CrawWurm();
        CounselOfTheSoratami drawnSpell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(tooExpensive));
        harness.setLibrary(player1, List.of(drawnSpell));

        resolveCombatAndTrigger(player1);

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Counsel of the Soratami");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tooExpensive, drawnSpell);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(drawnSpell.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tooExpensive);
    }

    @Test
    @DisplayName("Combat damage still draws when no hand spell is cheap enough")
    void doesNotOfferSpellAboveDamage() {
        Permanent creature = addAttacker(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        CrawWurm tooExpensive = new CrawWurm();
        Forest drawnLand = new Forest();
        harness.setHand(player1, List.of(tooExpensive));
        harness.setLibrary(player1, List.of(drawnLand));

        resolveCombatAndTrigger(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tooExpensive, drawnLand);
    }

    @Test
    @DisplayName("The trigger does not fire when combat damage is dealt only to a creature")
    void noTriggerWhenBlocked() {
        Permanent creature = addAttacker(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.setLibrary(player1, List.of(new CounselOfTheSoratami()));

        Permanent blocker = addCreatureReady(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombatAndTrigger(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent-controlled Sword draws and casts for its own controller")
    void opponentControlledSwordUsesItsController() {
        Permanent creature = addAttacker(player1);
        Permanent sword = addSwordReady(player2);
        sword.setAttachedTo(creature.getId());
        CrawWurm tooExpensive = new CrawWurm();
        CounselOfTheSoratami drawnSpell = new CounselOfTheSoratami();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(tooExpensive));
        harness.setLibrary(player2, List.of(drawnSpell));

        resolveCombatAndTrigger(player1);

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(drawnSpell.getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(tooExpensive);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void equipTransfersBoostForTwoMana() {
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        Permanent sword = addSwordReady(player1);
        sword.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sword),
                null, second.getId());
        harness.passBothPriorities();

        assertThat(sword.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    void decliningCastKeepsDrawnCard() {
        Permanent creature = addAttacker(player1);
        addSwordReady(player1).setAttachedTo(creature.getId());
        CounselOfTheSoratami spell = new CounselOfTheSoratami();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn));

        resolveCombatAndTrigger(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spell, drawn);
    }

    @Test
    void castsCreatureAtExactDamageBoundaryWithoutMana() {
        Permanent creature = addAttacker(player1);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addSwordReady(player1).setAttachedTo(creature.getId());
        CrawWurm spell = new CrawWurm();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(spell));
        harness.setLibrary(player1, List.of(drawn));

        resolveCombatAndTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Craw Wurm").getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void castsOnlyOneOfMultipleEligibleSpells() {
        Permanent creature = addAttacker(player1);
        addSwordReady(player1).setAttachedTo(creature.getId());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(drawn));

        resolveCombatAndTrigger(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(first.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, drawn);
    }

    private Permanent addSwordReady(Player player) {
        return addCreatureReady(player, new BusterSword());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private Permanent addAttacker(Player player) {
        Permanent creature = addCreatureReady(player);
        creature.setAttacking(true);
        return creature;
    }

    private void resolveCombatAndTrigger(Player activePlayer) {
        resolveCombat(activePlayer);
        resolveAllTriggers();
    }
}
