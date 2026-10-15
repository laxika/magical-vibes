package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.c.CrashOfRhinos;
import com.github.laxika.magicalvibes.cards.c.ChandraLegacyOfFire;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fiendlash.class, CrashOfRhinos.class, Shock.class, ChandraLegacyOfFire.class, Abrade.class})
class FiendlashTest extends BaseCardTest {

    @Test
    @DisplayName("Fiendlash gives the equipped creature +2/+0 and reach")
    void equippedCreatureGetsBoostAndReach() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("When the equipped creature is dealt damage, it deals its power to a player")
    void damagedEquippedCreatureDealsItsPowerToTargetPlayer() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(creature.getId());

        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("Fiendlash's trigger cannot target a creature")
    void triggerCannotTargetCreature() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(creature.getId());
        Permanent otherCreature = addCreatureReady(player2, new CrashOfRhinos());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(player2.getId()).doesNotContain(otherCreature.getId());
    }

    @Test
    void equipPaysThreeManaAndMovesBoostAndReach() {
        Permanent first = addCreatureReady(player1, new CrashOfRhinos());
        Permanent second = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 2, null, second.getId());
        harness.passBothPriorities();

        assertThat(lash.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(10);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
    }

    @Test
    void triggerDealsDamageToPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        addLashReady().setAttachedTo(creature.getId());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraLegacyOfFire());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(planeswalker.getCard());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void lethalDamageStillUsesLastKnownEquippedPower() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        addLashReady().setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 40);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(30);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void triggerStillUsesOriginalCreatureAfterEquipmentIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        Permanent lash = addLashReady();
        lash.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock(), new Abrade()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castInstant(player1, 0, 1, lash.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(lash);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    void equipmentControllerChoosesTargetWhenOpponentControlsCreature() {
        Permanent creature = addCreatureReady(player2, new CrashOfRhinos());
        addLashReady().setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    void damageToUnequippedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new CrashOfRhinos());
        addLashReady();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent addLashReady() {
        return harness.addToBattlefieldAndReturn(player1, new Fiendlash());
    }
}
