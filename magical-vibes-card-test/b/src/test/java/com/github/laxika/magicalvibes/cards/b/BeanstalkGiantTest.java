package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DidntSayPlease;
import com.github.laxika.magicalvibes.cards.f.FertileFootsteps;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GingerbreadCabin;
import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeanstalkGiant.class, FertileFootsteps.class, Forest.class, SporecapSpider.class,
        GingerbreadCabin.class, DidntSayPlease.class})
class BeanstalkGiantTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualControllerLands() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new BeanstalkGiant());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void adventureSearchesForBasicLandToBattlefieldAndExilesCard() {
        Forest forest = new Forest();
        SporecapSpider filler = new SporecapSpider();
        BeanstalkGiant card = new BeanstalkGiant();
        harness.setLibrary(player1, List.of(filler, forest));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent fetchedForest = findPermanent(player1, "Forest");
        assertThat(fetchedForest.getCard()).isSameAs(forest);
        assertThat(fetchedForest.isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventure() {
        BeanstalkGiant card = new BeanstalkGiant();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Beanstalk Giant");
        assertThat(giant.getCard()).isSameAs(card);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureCanFailToFindEvenWithBasicLandAvailable() {
        BeanstalkGiant card = new BeanstalkGiant();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureDoesNotFindNonbasicForest() {
        BeanstalkGiant card = new BeanstalkGiant();
        GingerbreadCabin cabin = new GingerbreadCabin();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(cabin));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(cabin);
        harness.assertNotOnBattlefield(player1, "Gingerbread Cabin");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureWithEmptyLibraryStillGrantsCreatureCastingPermission() {
        BeanstalkGiant card = new BeanstalkGiant();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void losingLastLandMakesGiantDieWithZeroToughness() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new BeanstalkGiant());
        harness.addToBattlefield(player2, new Forest());
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Beanstalk Giant");
        harness.assertInGraveyard(player1, "Beanstalk Giant");
    }

    @Test
    void counteredAdventureGoesToGraveyardWithoutExilePermission() {
        BeanstalkGiant card = new BeanstalkGiant();
        harness.setHand(player1, List.of(card));
        harness.setHand(player2, List.of(new DidntSayPlease()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, gd.stack.getFirst().getCard().getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void castingCreatureDirectlyDoesNotSearchLibrary() {
        BeanstalkGiant card = new BeanstalkGiant();
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new GingerbreadCabin());
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent giant = findPermanent(player1, "Beanstalk Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
