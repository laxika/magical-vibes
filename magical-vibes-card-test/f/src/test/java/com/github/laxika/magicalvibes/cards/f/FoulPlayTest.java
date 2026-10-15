package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HarvesttideSentry;
import com.github.laxika.magicalvibes.cards.m.MightOfTheOldWays;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SearchPartyCaptain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoulPlay.class, HarvesttideSentry.class, Plains.class,
        SearchPartyCaptain.class, MightOfTheOldWays.class})
class FoulPlayTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with power 2 or less and investigates")
    void destroysSmallCreatureAndInvestigates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SearchPartyCaptain());
        castFoulPlay(target);

        harness.assertNotOnBattlefield(player2, "Search Party Captain");
        harness.assertInGraveyard(player2, "Search Party Captain");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 2")
    void cannotTargetLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HarvesttideSentry());
        prepareFoulPlay();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareFoulPlay();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 2 or less");
    }

    @Test
    @DisplayName("Can destroy its controller's creature and investigate")
    void destroysOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SearchPartyCaptain());
        castFoulPlay(target);

        harness.assertInGraveyard(player1, "Search Party Captain");
        harness.assertNotOnBattlefield(player1, "Search Party Captain");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not investigate if the target's power exceeds two before resolution")
    void doesNotInvestigateWhenTargetGrows() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SearchPartyCaptain());
        prepareFoulPlay();
        harness.castSorcery(player1, 0, target.getId());
        harness.setHand(player2, List.of(new MightOfTheOldWays()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Search Party Captain");
        harness.assertNotInGraveyard(player2, "Search Party Captain");
        harness.assertInGraveyard(player1, "Foul Play");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Investigates even when indestructible prevents destruction")
    void investigatesWhenDestructionIsPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SearchPartyCaptain());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        castFoulPlay(target);

        harness.assertOnBattlefield(player2, "Search Party Captain");
        harness.assertNotInGraveyard(player2, "Search Party Captain");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The investigated Clue can be sacrificed for two mana to draw a card")
    void clueCanBeSacrificedToDraw() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SearchPartyCaptain());
        harness.setLibrary(player1, List.of(new Plains()));
        castFoulPlay(target);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertNotInHand(player1, "Plains");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Plains");
    }

    private void castFoulPlay(Permanent target) {
        prepareFoulPlay();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void prepareFoulPlay() {
        harness.setHand(player1, List.of(new FoulPlay()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
