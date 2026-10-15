package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.k.KolaghansCommand;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.SearingBlaze;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImodaneThePyrohammer.class, GrizzlyBears.class, Pyroclasm.class, SearingBlaze.class,
        Shock.class, DarksteelMyr.class, KolaghansCommand.class})
class ImodaneThePyrohammerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the creature damage to each opponent when a single-target creature spell deals damage")
    void singleTargetCreatureSpellDealsDamageToEachOpponent() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when an instant or sorcery targets a player")
    void playerTargetDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger for an untargeted damage spell")
    void untargetedDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pyroclasm()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, (java.util.UUID) null);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when the damage spell has another target")
    void multipleTargetsDoNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SearingBlaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), creature.getId()));
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Damage to a friendly creature also triggers Imodane")
    void friendlyCreatureDamageTriggers() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's damage spell does not trigger Imodane")
    void opposingSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A spell whose creature target dies in response does not trigger Imodane")
    void illegalCreatureTargetDoesNotTrigger() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Lethal damage to Imodane triggers and resolves after she dies")
    void lethalDamageToImodaneStillTriggers() {
        Permanent imodane = addCreatureReady(player1, new ImodaneThePyrohammer());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, imodane.getId());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, imodane.getId());
        harness.assertInGraveyard(player1, "Imodane, the Pyrohammer");
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Targeting the same creature with two modes still triggers Imodane")
    void repeatedTargetOfSameCreatureTriggers() {
        harness.addToBattlefield(player1, new ImodaneThePyrohammer());
        Permanent target = addCreatureReady(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new KolaghansCommand()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{2, 3}, null,
                List.of(target.getId(), target.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
