package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PersistentPetitioners;
import com.github.laxika.magicalvibes.cards.r.RaulTroubleShooter;
import com.github.laxika.magicalvibes.cards.w.WindingWay;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TatoFarmer.class, Forest.class, PersistentPetitioners.class,
        RaulTroubleShooter.class, WindingWay.class})
class TatoFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall may give its controller two rad counters")
    void landfallMayGiveTwoRadCounters() {
        harness.addToBattlefield(player1, new TatoFarmer());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining landfall gives no rad counters")
    void decliningLandfallGivesNoRadCounters() {
        harness.addToBattlefield(player1, new TatoFarmer());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
    }

    @Test
    @DisplayName("Tap ability returns a milled land from any graveyard tapped")
    void returnsMilledLandTappedUnderControllerControl() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new TatoFarmer());
        farmer.setSummoningSick(false);

        for (int i = 0; i < 4; i++) {
            Permanent petitioners = harness.addToBattlefieldAndReturn(player2, new PersistentPetitioners());
            petitioners.setSummoningSick(false);
        }
        Forest milledLand = new Forest();
        harness.setLibrary(player2, List.of(milledLand));
        harness.activateAbility(player2, 3, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, milledLand.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(milledLand.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(milledLand);
    }

    @Test
    @DisplayName("Tap ability cannot target a land that was not milled this turn")
    void cannotTargetLandNotMilledThisTurn() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new TatoFarmer());
        farmer.setSummoningSick(false);
        Forest land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's land entering does not trigger landfall")
    void opponentsLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new TatoFarmer());

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
        assertThat(gd.playerRadCounters.get(player2.getId())).isNull();
    }

    @Test
    @DisplayName("Returning your own milled land triggers landfall and pays the tap cost")
    void returningOwnMilledLandTriggersLandfall() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new TatoFarmer());
        farmer.setSummoningSick(false);
        Permanent raul = harness.addToBattlefieldAndReturn(player1, new RaulTroubleShooter());
        raul.setSummoningSick(false);
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);

        harness.activateAbility(player1, 0, null, land.getId(), Zone.GRAVEYARD);
        assertThat(farmer.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(land.getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("A milled nonland card is not a legal target")
    void cannotTargetMilledNonland() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new TatoFarmer());
        farmer.setSummoningSick(false);
        Permanent raul = harness.addToBattlefieldAndReturn(player1, new RaulTroubleShooter());
        raul.setSummoningSick(false);
        TatoFarmer nonland = new TatoFarmer();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(nonland));
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(nonland);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonland.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(farmer.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(nonland);
    }

    @Test
    @DisplayName("A summoning-sick farmer cannot return a legally milled land")
    void summoningSicknessPreventsTapAbility() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new TatoFarmer());
        farmer.setSummoningSick(true);
        Permanent raul = harness.addToBattlefieldAndReturn(player1, new RaulTroubleShooter());
        raul.setSummoningSick(false);
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(land));
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(farmer.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("Putting a land into a graveyard from a library without milling does not make it targetable")
    void cannotTargetLandPutIntoGraveyardWithoutMilling() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new TatoFarmer());
        farmer.setSummoningSick(false);
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(land, new TatoFarmer(), new TatoFarmer(), new TatoFarmer()));
        harness.setHand(player1, List.of(new WindingWay()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleListChoice(player1, CardType.CREATURE.name());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(farmer.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
    }
}
