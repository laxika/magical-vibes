package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExcaliburSwordOfEden;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StaffOfEdenVaultsKey.class, IsamaruHoundOfKonda.class, GrizzlyBears.class,
        ExcaliburSwordOfEden.class, GrafdiggersCage.class})
class StaffOfEdenVaultsKeyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted legendary permanent from any graveyard")
    void etbReturnsTargetedLegendaryPermanentFromAnyGraveyard() {
        Card legendaryPermanent = new IsamaruHoundOfKonda();
        harness.setGraveyard(player2, List.of(legendaryPermanent));
        harness.setHand(player1, List.of(new StaffOfEdenVaultsKey()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legendaryPermanent.getId());

        harness.handleMultipleCardsChosen(player1, List.of(legendaryPermanent.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertNotInGraveyard(player2, "Isamaru, Hound of Konda");
    }

    @Test
    @DisplayName("ETB excludes nonlegendary permanents and cards named Staff of Eden")
    void etbExcludesIllegalGraveyardCards() {
        Card nonlegendaryPermanent = new GrizzlyBears();
        Card sameName = new StaffOfEdenVaultsKey();
        harness.setGraveyard(player1, List.of(nonlegendaryPermanent, sameName));
        harness.setHand(player1, List.of(new StaffOfEdenVaultsKey()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Staff of Eden, Vault's Key");
    }

    @Test
    @DisplayName("Tap ability draws for permanents controlled but not owned")
    void tapAbilityDrawsForPermanentsControlledButNotOwned() {
        Permanent staff = addStaffReady();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card stolenCard = new GrizzlyBears();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolenPermanent = harness.addToBattlefieldAndReturn(player2, stolenCard);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class).applyControlEffect(
                gd,
                player1.getId(),
                stolenPermanent,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT,
                null,
                "Test Control Effect"
        ));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("ETB can return a legendary permanent from your own graveyard")
    void etbReturnsLegendaryPermanentFromOwnGraveyard() {
        Card legendaryPermanent = new IsamaruHoundOfKonda();
        legendaryPermanent.setOwnerId(player1.getId());
        harness.setGraveyard(player1, List.of(legendaryPermanent));
        harness.setHand(player1, List.of(new StaffOfEdenVaultsKey()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(legendaryPermanent.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertNotInGraveyard(player1, "Isamaru, Hound of Konda");
    }

    @Test
    @CardUsed({StaffOfEdenVaultsKey.class, ExcaliburSwordOfEden.class})
    @DisplayName("ETB returns a noncreature legendary permanent under your control")
    void etbReturnsOpponentsLegendaryEquipmentAndPreservesOwnership() {
        Card equipment = new ExcaliburSwordOfEden();
        equipment.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(equipment));
        harness.setHand(player1, List.of(new StaffOfEdenVaultsKey()));
        harness.setLibrary(player1, List.of(new StaffOfEdenVaultsKey()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Excalibur, Sword of Eden");
        harness.assertNotOnBattlefield(player2, "Excalibur, Sword of Eden");
        harness.assertNotInGraveyard(player2, "Excalibur, Sword of Eden");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB does not return a target that leaves the graveyard before resolution")
    void etbDoesNotReturnTargetThatLeftGraveyard() {
        Card legendaryPermanent = new IsamaruHoundOfKonda();
        legendaryPermanent.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(legendaryPermanent));
        harness.setHand(player1, List.of(new StaffOfEdenVaultsKey()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(legendaryPermanent.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(legendaryPermanent));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({StaffOfEdenVaultsKey.class, IsamaruHoundOfKonda.class, GrafdiggersCage.class})
    @DisplayName("A creature blocked by Grafdigger's Cage remains in its owner's graveyard")
    void blockedReanimationPreservesOpponentsGraveyard() {
        Card legendaryPermanent = new IsamaruHoundOfKonda();
        legendaryPermanent.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(legendaryPermanent));
        harness.addToBattlefield(player1, new GrafdiggersCage());
        harness.setHand(player1, List.of(new StaffOfEdenVaultsKey()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(legendaryPermanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(legendaryPermanent.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Isamaru, Hound of Konda");
        harness.assertNotOnBattlefield(player2, "Isamaru, Hound of Konda");
        harness.assertInGraveyard(player2, "Isamaru, Hound of Konda");
        harness.assertNotInGraveyard(player1, "Isamaru, Hound of Konda");
    }

    @Test
    @DisplayName("Tap ability draws nothing when every controlled permanent is owned")
    void tapAbilityDrawsZeroForOwnedPermanents() {
        Permanent staff = addStaffReady();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StaffOfEdenVaultsKey()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Tap ability counts an opponent-owned Staff and counts permanents at resolution")
    void tapAbilityCountsForeignStaffAndUsesResolutionCount() {
        Card staffCard = new StaffOfEdenVaultsKey();
        staffCard.setOwnerId(player2.getId());
        Permanent staff = harness.addToBattlefieldAndReturn(player1, staffCard);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new StaffOfEdenVaultsKey(),
                new StaffOfEdenVaultsKey(), new StaffOfEdenVaultsKey()));

        harness.activateAbility(player1, 0, null, null);
        Card foreignCreature = new GrizzlyBears();
        foreignCreature.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, foreignCreature);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card ownCreatureControlledByOpponent = new GrizzlyBears();
        ownCreatureControlledByOpponent.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, ownCreatureControlledByOpponent);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addStaffReady() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfEdenVaultsKey());
        staff.setSummoningSick(false);
        return staff;
    }

}
