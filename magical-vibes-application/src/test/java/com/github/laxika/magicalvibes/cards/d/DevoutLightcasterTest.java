package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.m.MireBlight;
import com.github.laxika.magicalvibes.cards.o.OranRiefSurvivalist;
import com.github.laxika.magicalvibes.cards.q.QuestForTheGravelord;
import com.github.laxika.magicalvibes.cards.v.VampireLacerator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DevoutLightcaster.class, QuestForTheGravelord.class, VampireLacerator.class,
        OranRiefSurvivalist.class, Disfigure.class, IntoTheRoil.class, MireBlight.class})
class DevoutLightcasterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles target black permanent")
    void etbExilesTargetBlackPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuestForTheGravelord());

        castLightcaster(target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Quest for the Gravelord");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a nonblack permanent")
    void cannotTargetNonblackPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OranRiefSurvivalist());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new VampireLacerator());

        harness.castFromHand(player1, new DevoutLightcaster(), "{W}{W}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Oran-Rief Survivalist");
        harness.assertNotOnBattlefield(player2, "Vampire Lacerator");
    }

    @Test
    @DisplayName("Protection from black prevents a black creature from blocking")
    void protectionFromBlackPreventsBlocking() {
        Permanent lightcaster = addCreatureReady(player1, new DevoutLightcaster());
        lightcaster.setAttacking(true);
        harness.addToBattlefield(player2, new VampireLacerator());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from black prevents black spells from targeting it")
    void protectionFromBlackPreventsTargeting() {
        Permanent lightcaster = harness.addToBattlefieldAndReturn(player2, new DevoutLightcaster());

        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, lightcaster.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Can cast without legal black permanents")
    void canCastWithoutLegalTargets() {
        harness.addToBattlefield(player2, new OranRiefSurvivalist());

        harness.castFromHand(player1, new DevoutLightcaster(), "{W}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devout Lightcaster");
        harness.assertOnBattlefield(player2, "Oran-Rief Survivalist");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB must target your own black permanent when it is the only legal target")
    void etbExilesOwnBlackPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new QuestForTheGravelord());

        harness.castFromHand(player1, new DevoutLightcaster(), "{W}{W}{W}");
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Quest for the Gravelord");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Entering without being cast still exiles a black creature")
    void enteringWithoutBeingCastTriggersExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VampireLacerator());

        harness.enterBattlefieldAndReturn(player1, new DevoutLightcaster());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vampire Lacerator");
        harness.assertNotInGraveyard(player2, "Vampire Lacerator");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("ETB does not exile a target that leaves before resolution")
    void targetLeavingBeforeResolutionIsNotExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VampireLacerator());
        harness.addToBattlefield(player2, new QuestForTheGravelord());

        harness.castFromHand(player1, new DevoutLightcaster(), "{W}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Vampire Lacerator");
        harness.assertOnBattlefield(player2, "Quest for the Gravelord");
        harness.assertOnBattlefield(player1, "Devout Lightcaster");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB still resolves after Devout Lightcaster leaves")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new QuestForTheGravelord());

        harness.castFromHand(player1, new DevoutLightcaster(), "{W}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Devout Lightcaster"));
        resolveAllTriggers();

        harness.assertInHand(player1, "Devout Lightcaster");
        harness.assertNotOnBattlefield(player2, "Quest for the Gravelord");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Protection from black prevents combat damage while blocking a black creature")
    void protectionFromBlackPreventsCombatDamage() {
        addCreatureReady(player1, new VampireLacerator());
        Permanent lightcaster = addCreatureReady(player2, new DevoutLightcaster());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Devout Lightcaster");
        assertThat(lightcaster.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Vampire Lacerator");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Protection from black prevents a black Aura from enchanting it")
    void protectionFromBlackPreventsBlackAura() {
        Permanent lightcaster = harness.addToBattlefieldAndReturn(player2, new DevoutLightcaster());
        harness.setHand(player1, List.of(new MireBlight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, lightcaster.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    private void castLightcaster(UUID targetId) {
        harness.setHand(player1, List.of(new DevoutLightcaster()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
