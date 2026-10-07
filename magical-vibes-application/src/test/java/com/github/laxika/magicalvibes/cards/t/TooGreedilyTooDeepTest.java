package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PurphorosGodOfTheForge;
import com.github.laxika.magicalvibes.cards.p.PyromancersGauntlet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TooGreedilyTooDeep.class, AirElemental.class, GrizzlyBears.class,
        PurphorosGodOfTheForge.class, PyromancersGauntlet.class})
class TooGreedilyTooDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and it damages each other creature")
    void returnsCreatureAndDamagesEachOtherCreature() {
        GrizzlyBears returnedCard = new GrizzlyBears();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareCast(returnedCard);

        harness.castAndResolveSorcery(player1, 0, returnedCard.getId());

        Permanent returnedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(returnedCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedPermanent.getMarkedDamage()).isZero();
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Only accepts a creature card as the graveyard target")
    void onlyAcceptsCreatureCardTarget() {
        TooGreedilyTooDeep nonCreatureTarget = new TooGreedilyTooDeep();
        harness.setGraveyard(player1, List.of(nonCreatureTarget));
        harness.setHand(player1, List.of(new TooGreedilyTooDeep()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreatureTarget.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns an opponent's creature under the caster's control and deals lethal damage")
    void returnsOpponentsCreatureUnderCastersControl() {
        AirElemental returnedCard = new AirElemental();
        harness.setGraveyard(player2, List.of(returnedCard));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TooGreedilyTooDeep()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, returnedCard.getId());

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does nothing when the only target leaves the graveyard before resolution")
    void doesNothingWhenTargetLeavesGraveyard() {
        GrizzlyBears returnedCard = new GrizzlyBears();
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareCast(returnedCard);
        harness.castSorcery(player1, 0, returnedCard.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(returnedCard));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(otherCreature.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Too Greedily, Too Deep");
    }

    @Test
    @DisplayName("Spell damage bonuses do not apply to the returned creature's damage")
    void spellDamageBonusDoesNotApply() {
        GrizzlyBears returnedCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        prepareCast(returnedCard);

        harness.castAndResolveSorcery(player1, 0, returnedCard.getId());

        assertThat(otherCreature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    @Test
    @DisplayName("A returned God that is not a creature deals no damage")
    void returnedNoncreatureDealsNoDamage() {
        PurphorosGodOfTheForge returnedCard = new PurphorosGodOfTheForge();
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new TooGreedilyTooDeep()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, returnedCard.getId());

        harness.assertOnBattlefield(player1, "Purphoros, God of the Forge");
        assertThat(otherCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Air Elemental");
    }

    private void prepareCast(GrizzlyBears returnedCard) {
        harness.setGraveyard(player1, List.of(returnedCard));
        harness.setHand(player1, List.of(new TooGreedilyTooDeep()));
        addMana();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
