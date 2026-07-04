package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiregrafCaptainTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has Zombie anthem and a targeted ally-Zombie-dies life-loss trigger")
    void hasCorrectStructure() {
        DiregrafCaptain card = new DiregrafCaptain();

        // Other Zombie creatures you control get +1/+1.
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);
        StaticBoostEffect boost = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);

        // Whenever another Zombie you control dies, target opponent loses 1 life.
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst())
                .isInstanceOf(TriggeringCardConditionalEffect.class);
        TriggeringCardConditionalEffect conditional =
                (TriggeringCardConditionalEffect) card.getEffects(EffectSlot.ON_ALLY_CREATURE_DIES).getFirst();
        assertThat(conditional.predicate()).isEqualTo(new CardSubtypePredicate(CardSubtype.ZOMBIE));
        assertThat(conditional.wrapped()).isInstanceOf(TargetPlayerLosesLifeEffect.class);
        assertThat(((TargetPlayerLosesLifeEffect) conditional.wrapped()).amount()).isEqualTo(1);

        // Trigger targets an opponent.
        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
        PlayerPredicateTargetFilter filter = (PlayerPredicateTargetFilter) card.getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PlayerRelationPredicate.class);
        assertThat(((PlayerRelationPredicate) filter.predicate()).relation()).isEqualTo(PlayerRelation.OPPONENT);
    }

    // ===== Static anthem =====

    @Test
    @DisplayName("Other Zombie creatures you control get +1/+1")
    void buffsOwnZombies() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new DiregrafCaptain());

        Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
    }

    @Test
    @DisplayName("Diregraf Captain does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new DiregrafCaptain());

        Permanent captain = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Diregraf Captain"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new DiregrafCaptain());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Death trigger =====

    @Test
    @DisplayName("When another Zombie you control dies, target opponent loses 1 life")
    void anotherZombieDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new DiregrafGhoul());

        int p2LifeBefore = gd.getLife(player2.getId());

        // Diregraf Ghoul is 2/2 base, 3/3 under the anthem — kill it with Lightning Bolt (3 damage).
        setupPlayer2Active();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID ghoulId = harness.getPermanentId(player1, "Diregraf Ghoul");
        harness.castInstant(player2, 0, ghoulId);
        harness.passBothPriorities(); // Bolt resolves → Ghoul dies → trigger awaits target

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        // Only the opponent (player2) is a valid target, never the controller (player1).
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve the life-loss trigger

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 1);
    }

    @Test
    @DisplayName("Does not trigger when a non-Zombie creature you control dies")
    void nonZombieDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new DiregrafCaptain());
        harness.addToBattlefield(player1, new GrizzlyBears());

        int p2LifeBefore = gd.getLife(player2.getId());

        // Grizzly Bears is not a Zombie, so the anthem doesn't apply — 2/2 dies to Shock.
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Shock resolves → Bears die, no trigger

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    @Test
    @DisplayName("Diregraf Captain's own death does not trigger its ability (\"another\" Zombie)")
    void ownDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new DiregrafCaptain());

        int p2LifeBefore = gd.getLife(player2.getId());

        // Diregraf Captain is 2/2 (does not buff itself) — dies to Shock.
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID captainId = harness.getPermanentId(player1, "Diregraf Captain");
        harness.castInstant(player2, 0, captainId);
        harness.passBothPriorities(); // Shock resolves → Captain dies, no trigger

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    // ===== Helpers =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
