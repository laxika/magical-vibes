package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AlabasterMage;
import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GarruksCompanion;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SacredWolf;
import com.github.laxika.magicalvibes.cards.s.SpiritMantle;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
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

@CardUsed({WarstormSurge.class, CanyonMinotaur.class, RuneclawBear.class,
        JaceBeleren.class, ChandraNalaar.class, ImprisonedInTheMoon.class,
        GarruksCompanion.class, TitanicGrowth.class, DoomBlade.class,
        AlabasterMage.class, SacredWolf.class, SpiritMantle.class, BirdsOfParadise.class})
class WarstormSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control entering deals damage equal to its power to a chosen player")
    void enteringCreatureDealsPowerDamageToPlayer() {
        harness.addToBattlefield(player1, new WarstormSurge());
        harness.setLife(player2, 20);

        GameData gd = harness.getGameData();
        harness.castFromHand(player1, new CanyonMinotaur(), "{3}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        // Canyon Minotaur is a 3/3, so it deals 3 damage — the entering creature is the source.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The entering creature's damage can be aimed at a creature and kill it")
    void enteringCreatureDealsPowerDamageToCreature() {
        harness.addToBattlefield(player1, new WarstormSurge());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());

        GameData gd = harness.getGameData();
        harness.castFromHand(player1, new CanyonMinotaur(), "{3}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(victim.getId()));
    }

    /**
     * The entering-permanent trigger's any-target enumeration is evaluated from the declared target
     * rather than re-implemented, so it reads the planeswalker type after layer 4 (CR 613.1d). A
     * planeswalker Imprisoned in the Moon turned into a colorless land is no longer an any target
     * (CR 115.4) — the same answer the spell path gives.
     */
    @Test
    @DisplayName("Offers a planeswalker, but not one Imprisoned in the Moon turned into a land")
    void offersPlaneswalkerUnlessLayerFourTookTheTypeAway() {
        harness.addToBattlefield(player1, new WarstormSurge());
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 6);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(jace.getId());

        harness.castFromHand(player1, new CanyonMinotaur(), "{3}{R}");
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(chandra.getId())
                .doesNotContain(jace.getId());
    }

    @Test
    @DisplayName("A creature an opponent controls entering does not trigger Warstorm Surge")
    void opponentCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new WarstormSurge());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        GameData gd = harness.getGameData();
        harness.castFromHand(player2, new CanyonMinotaur(), "{3}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage uses the entering creature's power when the trigger resolves")
    void usesPowerAtResolution() {
        harness.addToBattlefield(player1, new WarstormSurge());
        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof GarruksCompanion).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("The creature still deals damage using its last known power after it dies")
    void usesLastKnownPowerAfterCreatureDies() {
        harness.addToBattlefield(player1, new WarstormSurge());
        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof GarruksCompanion).findFirst().orElseThrow();
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Lifelink granted to the entering creature applies to the damage")
    void enteringCreatureLifelinkGainsLife() {
        harness.addToBattlefield(player1, new WarstormSurge());
        harness.addToBattlefield(player1, new AlabasterMage());
        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());

        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof GarruksCompanion).findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Protection from creatures allows the enchantment's target but prevents creature damage")
    void protectionFromCreaturesPreventsDamage() {
        harness.addToBattlefield(player1, new WarstormSurge());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SpiritMantle());
        aura.setAttachedTo(victim.getId());

        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        resolveAllTriggers();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(victim);
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An opponent's hexproof creature cannot be chosen as a target")
    void excludesOpponentsHexproofCreature() {
        harness.addToBattlefield(player1, new WarstormSurge());
        Permanent wolf = harness.addToBattlefieldAndReturn(player2, new SacredWolf());

        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player1.getId(), player2.getId())
                .doesNotContain(wolf.getId());
    }

    @Test
    @DisplayName("The entering creature can target itself")
    void enteringCreatureCanTargetItself() {
        harness.addToBattlefield(player1, new WarstormSurge());
        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        resolveAllTriggers();
        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof GarruksCompanion).findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c instanceof GarruksCompanion);
    }

    @Test
    @DisplayName("Damage to a planeswalker removes loyalty counters")
    void enteringCreatureDamagesPlaneswalker() {
        harness.addToBattlefield(player1, new WarstormSurge());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        harness.castFromHand(player1, new GarruksCompanion(), "{G}{G}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    @Test
    @DisplayName("A zero-power creature triggers but deals no damage")
    void zeroPowerCreatureDealsNoDamage() {
        harness.addToBattlefield(player1, new WarstormSurge());
        harness.castFromHand(player1, new BirdsOfParadise(), "{G}");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
