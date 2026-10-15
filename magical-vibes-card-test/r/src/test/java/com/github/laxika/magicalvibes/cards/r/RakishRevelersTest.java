package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BuyYourSilence;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakishRevelers.class, Island.class, BuyYourSilence.class})
class RakishRevelersTest extends BaseCardTest {

    @Test
    void entersAndCreatesGreenAndWhiteCitizenToken() {
        harness.castFromHand(player1, new RakishRevelers(), "{2}{R}{G}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent citizen = findPermanent(player1, "Citizen");
        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
        assertThat(citizen.getCard().isToken()).isTrue();
        assertThat(citizen.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(citizen.getEffectivePower()).isEqualTo(1);
        assertThat(citizen.getEffectiveToughness()).isEqualTo(1);
        assertThat(citizen.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(citizen.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    void handAbilityExilesTheCardAndGrantsOnlyRedGreenOrWhiteMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        RakishRevelers revelers = new RakishRevelers();
        harness.setHand(player1, List.of(revelers));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, land.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(revelers.getId())).isNotNull();

        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("RED", "GREEN", "WHITE");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void landGrantEndsWhenRakishRevelersIsCastFromExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        RakishRevelers revelers = new RakishRevelers();
        harness.setHand(player1, List.of(revelers));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        land.untap();

        harness.castFromExile(player1, revelers.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastWhenExiledByAnotherEffect() {
        RakishRevelers revelers = new RakishRevelers();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, revelers);
        harness.setHand(player1, List.of(new BuyYourSilence()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        assertThat(gd.findExiledCard(revelers.getId())).isNotNull();
        addRevelersCastingMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, revelers.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(revelers.getId())).isNotNull();
    }

    @Test
    void illegalLandTargetDoesNotGrantPermissionToCastFromExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        RakishRevelers revelers = new RakishRevelers();
        harness.setHand(player1, List.of(revelers));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.setGraveyard(player1, List.of(land.getCard()));
        harness.passBothPriorities();
        addRevelersCastingMana();

        assertThatThrownBy(() -> harness.castFromExile(player1, revelers.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(revelers.getId())).isNotNull();
    }

    @Test
    void canTargetOpponentsLandAndItsControllerReceivesTheMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new RakishRevelers()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "WHITE");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    void castingPermissionSurvivesTheLandLeavingAfterAbilityResolves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        RakishRevelers revelers = new RakishRevelers();
        harness.setHand(player1, List.of(revelers));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, land.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.setGraveyard(player1, List.of(land.getCard()));
        addRevelersCastingMana();

        harness.castFromExile(player1, revelers.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(revelers.getId())).isNull();
        assertThat(countPermanents(player1, "Rakish Revelers")).isEqualTo(1);
        assertThat(countPermanents(player1, "Citizen")).isEqualTo(1);
    }

    private void addRevelersCastingMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
