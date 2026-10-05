package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrimLavamancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SwiftfootBoots;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeoninLightbringer.class, Shock.class, SwiftfootBoots.class, GrimLavamancer.class})
class LeoninLightbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Leonin Lightbringer gets +1/+1 while equipped")
    void getsBonusWhileEquipped() {
        Permanent lightbringer = addLightbringer(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(lightbringer.getId());

        assertThat(gqs.getEffectivePower(gd, lightbringer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lightbringer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Leonin Lightbringer loses its bonus when it is no longer equipped")
    void losesBonusWhenEquipmentIsDetached() {
        Permanent lightbringer = addLightbringer(player1);
        Permanent equipment = addEquipment(player1);
        equipment.setAttachedTo(lightbringer.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, lightbringer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lightbringer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they do not pay")
    void wardCountersUnpaidSpell() {
        Permanent lightbringer = addLightbringer(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, lightbringer.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Leonin Lightbringer");
    }

    @Test
    @DisplayName("Paying Ward {2} lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent lightbringer = addLightbringer(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, lightbringer.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(lightbringer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void multipleEquipmentGrantOnlyOneBonus() {
        Permanent lightbringer = addLightbringer(player1);
        Permanent first = addEquipment(player1);
        Permanent second = addEquipment(player1);
        first.setAttachedTo(lightbringer.getId());
        second.setAttachedTo(lightbringer.getId());

        assertThat(gqs.getEffectivePower(gd, lightbringer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lightbringer)).isEqualTo(3);

        first.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, lightbringer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lightbringer)).isEqualTo(3);
    }

    @Test
    void ownSpellDoesNotTriggerWard() {
        Permanent lightbringer = addLightbringer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, lightbringer.getId());

        harness.assertInGraveyard(player1, "Leonin Lightbringer");
        harness.assertNotOnBattlefield(player1, "Leonin Lightbringer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningAffordableWardPaymentCountersSpell() {
        Permanent lightbringer = addLightbringer(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, lightbringer.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Leonin Lightbringer");
        assertThat(lightbringer.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardCountersOpposingActivatedAbility() {
        Permanent lightbringer = addLightbringer(player1);
        Permanent lavamancer = addCreatureReady(player2, new GrimLavamancer());
        harness.setGraveyard(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, lightbringer.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Leonin Lightbringer");
        assertThat(lightbringer.getMarkedDamage()).isZero();
        assertThat(lavamancer.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addLightbringer(Player player) {
        return addCreatureReady(player, new LeoninLightbringer());
    }

    private Permanent addEquipment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SwiftfootBoots());
    }
}
