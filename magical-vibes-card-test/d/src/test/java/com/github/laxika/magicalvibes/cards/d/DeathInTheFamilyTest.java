package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrayOgre;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathInTheFamily.class, Forest.class, GrayOgre.class, HillGiant.class})
class DeathInTheFamilyTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature with mana value 3 or less")
    void exilesSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayOgre());

        castDeathInTheFamily(target);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Gray Ogre");
    }

    @Test
    @DisplayName("Can exile your own creature and leaves other creatures untouched")
    void exilesOwnCreatureOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrayOgre());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrayOgre());

        castDeathInTheFamily(target);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player1, "Gray Ogre");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(other);
        harness.assertNotInGraveyard(player1, "Gray Ogre");
        harness.assertInGraveyard(player1, "Death in the Family");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles a stolen creature into its owner's exile zone")
    void exilesStolenCreatureForItsOwner() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayOgre());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        castDeathInTheFamily(target);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Gray Ogre");
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 3")
    void cannotTargetLargeCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        assertThatThrownBy(() -> castDeathInTheFamily(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castDeathInTheFamily(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with mana value 3 or less");
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrayOgre());
        prepareCast();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    private void castDeathInTheFamily(Permanent target) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new DeathInTheFamily()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
