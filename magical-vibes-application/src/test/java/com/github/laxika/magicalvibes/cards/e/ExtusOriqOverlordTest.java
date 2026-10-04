package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TeachByExample;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExtusOriqOverlord.class, GrizzlyBears.class, Shock.class})
class ExtusOriqOverlordTest extends BaseCardTest {

    @Test
    @CardUsed({EagerFirstYear.class})
    void frontFaceCannotSacrificeCreaturesToReduceItsCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setHand(player1, List.of(new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModesAndSacrifices(
                player1, 0, 1, 1, new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Eager First-Year");
        harness.assertInHand(player1, "Extus, Oriq Overlord");
    }

    @Test
    @CardUsed({EagerFirstYear.class})
    void discountedBackFaceIsOfferedAsPlayable() {
        harness.addToBattlefield(player1, new EagerFirstYear());
        harness.addToBattlefield(player1, new EagerFirstYear());
        harness.addToBattlefield(player1, new EagerFirstYear());
        harness.setHand(player1, List.of(new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    void magecraftCannotReturnLegendaryCreaturesOrInstants() {
        harness.addToBattlefield(player1, new ExtusOriqOverlord());
        harness.setGraveyard(player1, List.of(new ExtusOriqOverlord(), new Shock()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player1, "Extus, Oriq Overlord");
        harness.assertNotInHand(player1, "Extus, Oriq Overlord");
        harness.assertNotInHand(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({EagerFirstYear.class})
    void opponentsInstantDoesNotTriggerMagecraft() {
        harness.addToBattlefield(player1, new ExtusOriqOverlord());
        harness.setGraveyard(player1, List.of(new EagerFirstYear()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertInGraveyard(player1, "Eager First-Year");
        harness.assertNotInHand(player1, "Eager First-Year");
    }

    @Test
    @CardUsed({EagerFirstYear.class, TeachByExample.class})
    void copyingSorceryTriggersMagecraftSeparatelyFromCastingIt() {
        harness.addToBattlefield(player1, new ExtusOriqOverlord());
        harness.setHand(player1, List.of(new TeachByExample(), new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);
        EagerFirstYear first = new EagerFirstYear();
        EagerFirstYear second = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        harness.assertNotInGraveyard(player1, "Eager First-Year");
        assertThat(countPermanents(player1, "Avatar")).isEqualTo(2);
    }

    @Test
    void backFaceCreatesTokenWhenOpponentHasNoCreaturesAndNoCostsAreSacrificed() {
        harness.setHand(player1, List.of(new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar");
        harness.assertNotOnBattlefield(player1, "Extus, Oriq Overlord");
        harness.assertInGraveyard(player1, "Extus, Oriq Overlord");
    }

    @Test
    @CardUsed({EagerFirstYear.class})
    void threeSacrificesReduceBackFaceToItsColoredManaCost() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        harness.setHand(player1, List.of(new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalSorceryWithModesAndSacrifices(player1, 0, 1, 1, new int[]{1},
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eager First-Year");
        harness.assertOnBattlefield(player1, "Avatar");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard(), third.getCard());
    }

    @Test
    @CardUsed({EagerFirstYear.class})
    void castingSorceryTriggersMagecraft() {
        harness.addToBattlefield(player1, new ExtusOriqOverlord());
        EagerFirstYear target = new EagerFirstYear();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        harness.assertInHand(player1, "Eager First-Year");
        harness.assertNotInGraveyard(player1, "Eager First-Year");
        harness.assertOnBattlefield(player1, "Avatar");
    }

    @Test
    @CardUsed({EagerFirstYear.class})
    void magecraftCannotReturnOpponentsCreature() {
        harness.addToBattlefield(player1, new ExtusOriqOverlord());
        harness.setGraveyard(player2, List.of(new EagerFirstYear()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Eager First-Year");
        harness.assertNotInHand(player1, "Eager First-Year");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void extusDealsBothFirstStrikeAndRegularCombatDamage() {
        addCreatureReady(player1, new ExtusOriqOverlord());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Magecraft returns a nonlegendary creature card from the graveyard to hand")
    void magecraftReturnsNonlegendaryCreatureToHand() {
        harness.addToBattlefield(player1, new ExtusOriqOverlord());
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Awaken the Blood Avatar sacrifices an opponent creature and creates the attacking Avatar token")
    void awakenTheBloodAvatarCreatesAvatarToken() {
        Permanent sacrificedForCost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExtusOriqOverlord()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorceryWithModesAndSacrifices(
                player1, 0, 1, 1, new int[]{1}, List.of(sacrificedForCost.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Avatar");
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 6);
    }
}
