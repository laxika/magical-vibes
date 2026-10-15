package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CastOff;
import com.github.laxika.magicalvibes.cards.d.DidntSayPlease;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RealmCloakedGiant.class, CastOff.class, YouthfulKnight.class, GoldenEgg.class, DidntSayPlease.class})
class RealmCloakedGiantTest extends BaseCardTest {

    @Test
    void adventureDestroysNonGiantCreaturesAndExilesTheCard() {
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.addToBattlefield(player2, new YouthfulKnight());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new RealmCloakedGiant());
        RealmCloakedGiant card = new RealmCloakedGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player2, "Youthful Knight");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).contains(giant);
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        RealmCloakedGiant card = new RealmCloakedGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Realm-Cloaked Giant");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
    }

    @Test
    void castingCreatureFromHandDoesNotDestroyOtherCreatures() {
        harness.addToBattlefield(player2, new YouthfulKnight());

        harness.castFromHand(player1, new RealmCloakedGiant(), "{5}{W}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Realm-Cloaked Giant");
        harness.assertOnBattlefield(player2, "Youthful Knight");
    }

    @Test
    void adventurePreservesOwnGiantAndNoncreatureArtifacts() {
        harness.addToBattlefield(player1, new RealmCloakedGiant());
        harness.addToBattlefield(player2, new GoldenEgg());
        RealmCloakedGiant card = new RealmCloakedGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Realm-Cloaked Giant");
        harness.assertOnBattlefield(player2, "Golden Egg");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void counteredAdventureGoesToGraveyardWithoutExilePermission() {
        harness.addToBattlefield(player2, new YouthfulKnight());
        RealmCloakedGiant card = new RealmCloakedGiant();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.setHand(player2, List.of(new DidntSayPlease()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, gd.stack.getFirst().getTargetableId());

        harness.assertInGraveyard(player1, "Realm-Cloaked Giant");
        harness.assertOnBattlefield(player2, "Youthful Knight");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
        assertThat(harness.getGameData().exilePlayPermissions).doesNotContainKey(card.getId());
    }
}
