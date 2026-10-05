package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BrokenWings;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrayerOfBinding.class, SavannahLions.class, Forest.class, BrokenWings.class})
class PrayerOfBindingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles an opposing nonland permanent and gains 2 life")
    void exilesOpposingNonlandPermanentAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        int lifeBefore = gd.getLife(player1.getId());

        castAndResolve(target.getId());

        harness.assertNotOnBattlefield(player2, "Savannah Lions");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Savannah Lions"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Exiled permanent returns when Prayer of Binding leaves")
    void exiledPermanentReturnsWhenSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        castAndResolve(target.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        UUID prayerId = harness.getPermanentId(player1, "Prayer of Binding");
        harness.castAndResolveInstant(player2, 0, prayerId);

        harness.assertOnBattlefield(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("May enter without exiling a permanent and still gains 2 life")
    void mayEnterWithoutTargetAndStillGainsLife() {
        int lifeBefore = gd.getLife(player1.getId());

        castAndResolve(null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        harness.assertOnBattlefield(player1, "Prayer of Binding");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SavannahLions());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Removing Prayer before its trigger resolves prevents exile but still gains life")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        int lifeBefore = gd.getLife(player1.getId());
        prepareCast();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Prayer of Binding"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prayer of Binding");
        harness.assertOnBattlefield(player2, "Savannah Lions");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("An illegal chosen target prevents the entire trigger from resolving")
    void targetLeavesBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrayerOfBinding());
        int lifeBefore = gd.getLife(player1.getId());
        prepareCast();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new BrokenWings()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prayer of Binding");
        harness.assertOnBattlefield(player1, "Prayer of Binding");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Flash allows casting on the opponent's turn outside a main phase")
    void canCastDuringOpponentsUpkeep() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SavannahLions());
        int lifeBefore = gd.getLife(player1.getId());
        prepareCast();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prayer of Binding");
        harness.assertNotOnBattlefield(player2, "Savannah Lions");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Savannah Lions"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    private void castAndResolve(UUID targetId) {
        prepareCast();
        if (targetId == null) {
            harness.castEnchantment(player1, 0);
        } else {
            harness.castEnchantment(player1, 0, targetId);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PrayerOfBinding()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
