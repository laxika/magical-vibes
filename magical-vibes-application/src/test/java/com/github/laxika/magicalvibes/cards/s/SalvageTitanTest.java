package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfGrixis;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.r.RelicOfProgenitus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvageTitan.class, ObeliskOfEsper.class, ObeliskOfBant.class, ObeliskOfGrixis.class,
        RelicOfProgenitus.class, PullFromEternity.class})
class SalvageTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by sacrificing three artifacts instead of paying mana")
    void castBySacrificingThreeArtifacts() {
        UUID esper = harness.addToBattlefieldAndReturn(player1, new ObeliskOfEsper()).getId();
        UUID bant = harness.addToBattlefieldAndReturn(player1, new ObeliskOfBant()).getId();
        UUID grixis = harness.addToBattlefieldAndReturn(player1, new ObeliskOfGrixis()).getId();

        harness.setHand(player1, List.of(new SalvageTitan()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreatureWithAlternateCost(player1, 0, List.of(esper, bant, grixis));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Salvage Titan");
        harness.assertNotOnBattlefield(player1, "Obelisk of Esper");
        harness.assertNotOnBattlefield(player1, "Obelisk of Bant");
        harness.assertNotOnBattlefield(player1, "Obelisk of Grixis");

        // The alternate cost leaves the mana pool untouched.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
    }

    @Test
    @DisplayName("Graveyard ability returns Salvage Titan to hand and exiles the chosen artifact cards")
    void graveyardAbilityReturnsSelf() {
        ObeliskOfEsper first = new ObeliskOfEsper();
        ObeliskOfBant second = new ObeliskOfBant();
        ObeliskOfGrixis third = new ObeliskOfGrixis();
        ObeliskOfEsper unchosen = new ObeliskOfEsper();
        SalvageTitan titan = new SalvageTitan();
        harness.setGraveyard(player1, List.of(first, second, third, unchosen, titan));

        harness.activateGraveyardAbility(player1, 4);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Salvage Titan");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third)
                .doesNotContain(unchosen, titan);
    }

    @Test
    @DisplayName("Graveyard ability cannot be activated without three artifact cards to exile")
    void graveyardAbilityRequiresThreeArtifacts() {
        SalvageTitan titan = new SalvageTitan();
        harness.setGraveyard(player1, List.of(new ObeliskOfEsper(), titan));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard to exile");

        harness.assertInGraveyard(player1, "Salvage Titan");
    }

    @Test
    @DisplayName("Can exile Salvage Titan itself to pay its graveyard ability without returning it")
    void canExileSelfAsGraveyardAbilityCost() {
        ObeliskOfEsper first = new ObeliskOfEsper();
        ObeliskOfBant second = new ObeliskOfBant();
        SalvageTitan titan = new SalvageTitan();
        harness.setGraveyard(player1, List.of(first, second, titan));

        harness.activateGraveyardAbility(player1, 2);
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Salvage Titan");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, titan);
    }

    @Test
    @DisplayName("Can pay the printed mana cost without sacrificing artifacts")
    void castWithManaWithoutArtifacts() {
        harness.setHand(player1, List.of(new SalvageTitan()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Salvage Titan");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Graveyard ability returns only its source when another Titan is in the graveyard")
    void returnsOnlyActivatedTitan() {
        ObeliskOfEsper first = new ObeliskOfEsper();
        ObeliskOfBant second = new ObeliskOfBant();
        ObeliskOfGrixis third = new ObeliskOfGrixis();
        SalvageTitan titan = new SalvageTitan();
        SalvageTitan other = new SalvageTitan();
        harness.setGraveyard(player1, List.of(first, second, third, titan, other));

        harness.activateGraveyardAbility(player1, 3);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        harness.assertNotInHand(player1, "Salvage Titan");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(titan).doesNotContain(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @CardUsed({SalvageTitan.class, ObeliskOfEsper.class, ObeliskOfBant.class, ObeliskOfGrixis.class,
            RelicOfProgenitus.class, PullFromEternity.class})
    @DisplayName("An old graveyard ability cannot return a Titan that left and reentered the graveyard")
    void doesNotReturnTitanAfterLeavingAndReenteringGraveyard() {
        ObeliskOfEsper first = new ObeliskOfEsper();
        ObeliskOfBant second = new ObeliskOfBant();
        ObeliskOfGrixis third = new ObeliskOfGrixis();
        SalvageTitan titan = new SalvageTitan();
        harness.setGraveyard(player1, List.of(first, second, third, new ObeliskOfEsper(), titan));
        harness.addToBattlefield(player2, new RelicOfProgenitus());
        harness.setLibrary(player2, List.of(new ObeliskOfBant()));
        harness.setHand(player1, List.of(new PullFromEternity()));

        harness.activateGraveyardAbility(player1, 4);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(titan);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, titan.getId());
        harness.assertInGraveyard(player1, "Salvage Titan");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Salvage Titan");
        harness.assertNotInHand(player1, "Salvage Titan");
    }
}
