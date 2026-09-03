package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarkBanishing;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.r.RitualOfSteel;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrimFeast.class, DarkBanishing.class, FeralShadow.class, Incinerate.class,
        RitualOfSteel.class, ZhalfirinKnight.class})
class GrimFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller at the beginning of their upkeep")
    void dealsOneDamageAtControllerUpkeep() {
        harness.addToBattlefield(player1, new GrimFeast());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Does not trigger at the opponent's upkeep")
    void doesNotTriggerAtOpponentUpkeep() {
        harness.addToBattlefield(player1, new GrimFeast());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Controller gains life equal to the toughness of a dying opponent creature")
    void gainsLifeEqualToDyingOpponentCreatureToughness() {
        harness.addToBattlefield(player1, new GrimFeast());
        harness.addToBattlefield(player2, new FeralShadow());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID shadowId = harness.getPermanentId(player2, "Feral Shadow");
        harness.castInstant(player1, 0, shadowId);
        resolveAllTriggers();

        // Feral Shadow is 2/1; the gain follows toughness, not power.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Does not trigger when the controller's own creature dies")
    void doesNotTriggerOnOwnCreatureDeath() {
        harness.addToBattlefield(player1, new GrimFeast());
        harness.addToBattlefield(player1, new FeralShadow());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Incinerate()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID shadowId = harness.getPermanentId(player1, "Feral Shadow");
        harness.castInstant(player2, 0, shadowId);
        harness.passBothPriorities(); // Incinerate resolves, Feral Shadow dies

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Triggers when a creature I control is put into an opponent's graveyard")
    void triggersForOpponentGraveyardWhenControllerIsNotOpponent() {
        harness.addToBattlefield(player1, new GrimFeast());
        FeralShadow creatureCard = new FeralShadow();
        creatureCard.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, creatureCard);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID shadowId = harness.getPermanentId(player1, "Feral Shadow");
        harness.castInstant(player1, 0, shadowId);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Does not trigger when a controller-owned creature is put into that controller's graveyard")
    void doesNotTriggerForControllerGraveyardWhenControllerIsOpponent() {
        harness.addToBattlefield(player1, new GrimFeast());
        FeralShadow creatureCard = new FeralShadow();
        creatureCard.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, creatureCard);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID shadowId = harness.getPermanentId(player2, "Feral Shadow");
        harness.castInstant(player1, 0, shadowId);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("Uses the dying creature's last-known effective toughness")
    void usesLastKnownEffectiveToughness() {
        harness.addToBattlefield(player1, new GrimFeast());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZhalfirinKnight());
        Permanent ritual = new Permanent(new RitualOfSteel());
        ritual.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(ritual);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.setHand(player1, List.of(new DarkBanishing()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
    }
}
