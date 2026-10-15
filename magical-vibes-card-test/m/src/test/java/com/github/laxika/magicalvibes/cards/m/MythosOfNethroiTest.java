package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FightAsOne;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MythosOfNethroi.class, GrizzlyBears.class, MindStone.class, Forest.class, FightAsOne.class})
class MythosOfNethroiTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature without spending green and white mana")
    void destroysCreatureWithoutGreenAndWhiteMana() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithMana(ManaColor.BLACK, ManaColor.COLORLESS);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not destroy a noncreature permanent without spending green and white mana")
    void doesNotDestroyNoncreatureWithoutGreenAndWhiteMana() {
        var target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castWithMana(ManaColor.BLACK, ManaColor.COLORLESS);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Destroys a noncreature permanent when green and white mana were spent")
    void destroysNoncreatureWithGreenAndWhiteMana() {
        var target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new MythosOfNethroi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MythosOfNethroi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    private void castWithMana(ManaColor firstGeneric, ManaColor secondGeneric) {
        harness.setHand(player1, List.of(new MythosOfNethroi()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, firstGeneric, 1);
        harness.addMana(player1, secondGeneric, 1);
    }

    @Test
    void doesNotDestroyNoncreatureWhenOnlyGreenWasSpent() {
        var target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castWithMana(ManaColor.GREEN, ManaColor.COLORLESS);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Mind Stone");
        harness.assertInGraveyard(player1, "Mythos of Nethroi");
    }

    @Test
    void doesNotDestroyNoncreatureWhenOnlyWhiteWasSpent() {
        var target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castWithMana(ManaColor.WHITE, ManaColor.COLORLESS);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Mind Stone");
        harness.assertInGraveyard(player1, "Mythos of Nethroi");
    }

    @Test
    void stillDestroysCreatureWhenGreenAndWhiteWereSpent() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithMana(ManaColor.GREEN, ManaColor.WHITE);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void canDestroyOwnNoncreatureWhenGreenAndWhiteWereSpent() {
        var target = harness.addToBattlefieldAndReturn(player1, new MindStone());
        castWithMana(ManaColor.GREEN, ManaColor.WHITE);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
    }

    @Test
    void doesNotDestroyCreatureThatGainsIndestructibleInResponse() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithMana(ManaColor.GREEN, ManaColor.WHITE);
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new FightAsOne()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{1}, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mythos of Nethroi");
    }

    @Test
    void cannotTargetLandEvenWhenGreenAndWhiteAreAvailable() {
        var target = harness.addToBattlefieldAndReturn(player2, new Forest());
        castWithMana(ManaColor.GREEN, ManaColor.WHITE);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    void greenAndWhiteAddedAfterCastingDoNotEnableDestruction() {
        var target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        castWithMana(ManaColor.BLACK, ManaColor.COLORLESS);
        harness.castInstant(player1, 0, target.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mind Stone");
        harness.assertInGraveyard(player1, "Mythos of Nethroi");
    }
}
