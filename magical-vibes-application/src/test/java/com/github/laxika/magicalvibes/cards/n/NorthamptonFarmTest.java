package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HanweirBattlements;
import com.github.laxika.magicalvibes.cards.h.HanweirGarrison;
import com.github.laxika.magicalvibes.cards.h.HanweirTheWrithingTownship;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NorthamptonFarm.class, Forest.class, GrizzlyBears.class,
        HanweirBattlements.class, HanweirGarrison.class, HanweirTheWrithingTownship.class})
class NorthamptonFarmTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: Add {C}")
    void tapsForColorless() {
        harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles a creature you own and tracks it with Northampton Farm")
    void exilesOwnedCreature() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(farm.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot exile a creature you do not own")
    void cannotExileUnownedCreature() {
        harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing the Farm returns one exiled creature and other exiled cards to their owners' hands")
    void sacrificeReturnsCreatureAndOtherCards() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        Card otherCard = new Forest();
        gd.addToExile(player1.getId(), otherCard, farm.getId());
        farm.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Northampton Farm");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.getCardsExiledByPermanent(farm.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile an owned creature controlled by an opponent")
    void exilesOwnedCreatureUnderOpponentControl() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(farm.getId())).containsExactly(bears.getOriginalCard());
        assertThat(gd.findExiledCard(bears.getOriginalCard().getId()).ownerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Cannot exile an owned noncreature permanent")
    void cannotExileNoncreature() {
        harness.addToBattlefield(player1, new NorthamptonFarm());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Chooses one exiled creature during resolution and returns the other to hand")
    void choosesOneOfMultipleExiledCreatures() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, first.getId());
        harness.passBothPriorities();
        farm.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, second.getId());
        harness.passBothPriorities();
        farm.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.assertInGraveyard(player1, "Northampton Farm");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(second.getOriginalCard().getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(second.getOriginalCard().getId()))
                .noneMatch(p -> p.getCard().getId().equals(first.getOriginalCard().getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(first.getOriginalCard())
                .doesNotContain(second.getOriginalCard());
        assertThat(gd.getCardsExiledByPermanent(farm.getId())).isEmpty();
    }

    @Test
    @DisplayName("Still returns other exiled cards to their owners when no creature card is available")
    void returnsNoncreatureWithoutCreatureCard() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Card forest = new Forest();
        gd.addToExile(player2.getId(), forest, farm.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Northampton Farm");
        assertThat(gd.playerHands.get(player2.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.getCardsExiledByPermanent(farm.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a Farm does not return cards exiled with another Farm")
    void keepsDifferentFarmsExiledCardsSeparate() {
        Permanent firstFarm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent secondFarm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, first.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, 1, null, second.getId());
        harness.passBothPriorities();
        firstFarm.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(first.getOriginalCard().getId()))
                .noneMatch(p -> p.getCard().getId().equals(second.getOriginalCard().getId()));
        assertThat(gd.getCardsExiledByPermanent(firstFarm.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(secondFarm.getId())).containsExactly(second.getOriginalCard());
    }

    @Test
    @DisplayName("Exiling a melded creature links both physical cards and returns their front faces")
    void returnsMeldComponentsInsteadOfCombinedBackFace() {
        Permanent farm = harness.addToBattlefieldAndReturn(player1, new NorthamptonFarm());
        Permanent battlements = harness.addToBattlefieldAndReturn(player1, new HanweirBattlements());
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new HanweirGarrison());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 1, 2, null, null);
        harness.passBothPriorities();
        Permanent township = findPermanent(player1, "Hanweir, the Writhing Township");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, township.getId());
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(farm.getId()))
                .containsExactlyInAnyOrder(battlements.getOriginalCard(), garrison.getOriginalCard());
        farm.untap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hanweir Garrison");
        harness.assertNotOnBattlefield(player1, "Hanweir, the Writhing Township");
        assertThat(gd.playerHands.get(player1.getId())).contains(battlements.getOriginalCard());
        assertThat(gd.exiledCards).isEmpty();
    }
}
