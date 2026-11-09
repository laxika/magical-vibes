package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BelovedChaplain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.cards.h.HagraCrocodile;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Electropotence.class, HillGiant.class, GrizzlyBears.class, GiantScorpion.class,
        HagraCrocodile.class, Panharmonicon.class, LightningBolt.class, VampireNighthawk.class, BelovedChaplain.class})
class ElectropotenceTest extends BaseCardTest {

    private void resolveUntilInputOrEmpty() {
        for (int i = 0; i < 12; i++) {
            GameData gd = harness.getGameData();
            if (gd.interaction.isAwaitingInput() || gd.stack.isEmpty()) {
                return;
            }
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("A paid trigger deals the entering creature's power to a chosen player")
    void dealsEnteringCreaturePowerDamageToPlayer() {
        harness.addToBattlefield(player1, new Electropotence());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        GameData gd = harness.getGameData();
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The entering creature's power damage can destroy a creature")
    void dealsEnteringCreaturePowerDamageToCreature() {
        harness.addToBattlefield(player1, new Electropotence());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        GameData gd = harness.getGameData();
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, victim.getId());
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(victim.getId()));
    }

    @Test
    @DisplayName("Declining the payment prevents the damage")
    void decliningPaymentPreventsDamage() {
        harness.addToBattlefield(player1, new Electropotence());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        GameData gd = harness.getGameData();
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void usesPowerAtResolution() {
        harness.addToBattlefield(player1, new Electropotence());
        harness.setHand(player1, List.of(new HagraCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof HagraCrocodile).findFirst().orElseThrow();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 15);
    }

    @Test
    void enteringCreatureDeathtouchAppliesToDamage() {
        harness.addToBattlefield(player1, new Electropotence());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HagraCrocodile());
        victim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new GiantScorpion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, victim.getId());
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInGraveyard(player2, "Hagra Crocodile");
    }

    @Test
    void panharmoniconCreatesTwoIndependentlyPayableTriggers() {
        harness.addToBattlefield(player1, new Electropotence());
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new HagraCrocodile()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, true);
        resolveUntilInputOrEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 14);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A creature that left before the ability resolves deals damage equal to its last-known power, with its lifelink")
    void departedCreatureStillDealsItsDamage() {
        harness.addToBattlefield(player1, new Electropotence());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new VampireNighthawk(), new LightningBolt()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent nighthawk = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof VampireNighthawk).findFirst().orElseThrow();
        harness.castInstant(player1, 0, nighthawk.getId());
        resolveUntilInputOrEmpty();

        harness.assertInGraveyard(player1, "Vampire Nighthawk");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Electropotence is the ability's source: a creature with protection from creatures stays a legal target")
    void protectionFromCreaturesTargetStaysLegalButTheDamageIsPrevented() {
        harness.addToBattlefield(player1, new Electropotence());
        Permanent chaplain = harness.addToBattlefieldAndReturn(player2, new BelovedChaplain());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        resolveUntilInputOrEmpty();
        harness.handlePermanentChosen(player1, chaplain.getId());
        resolveUntilInputOrEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(chaplain.getMarkedDamage()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
