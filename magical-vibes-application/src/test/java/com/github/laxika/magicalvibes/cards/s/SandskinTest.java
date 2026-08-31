package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sandskin.class, FountainOfYouth.class, GoldMyr.class, GrizzlyBears.class, LightningBolt.class})
class SandskinTest extends BaseCardTest {

    @Test
    @DisplayName("Sandskin attaches to a targeted creature")
    void attachesToCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new Sandskin()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sandskin")
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Sandskin cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Sandskin()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sandskin prevents combat damage dealt by the enchanted creature")
    void preventsCombatDamageByEnchantedCreature() {
        harness.setLife(player2, 20);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent aura = new Permanent(new Sandskin());
        aura.setAttachedTo(attacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Sandskin prevents combat damage dealt to the enchanted creature")
    void preventsCombatDamageToEnchantedCreature() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GoldMyr());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent aura = new Permanent(new Sandskin());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gold Myr");
    }

    @Test
    @DisplayName("Sandskin does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new Sandskin());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        gs.playCard(gd, player1, 0, 0, bears.getId(), null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
