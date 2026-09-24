package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({StaffOfEdenVaultsKey.class, IsamaruHoundOfKonda.class, GrizzlyBears.class})
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

    private Permanent addStaffReady() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new StaffOfEdenVaultsKey());
        staff.setSummoningSick(false);
        return staff;
    }

}
