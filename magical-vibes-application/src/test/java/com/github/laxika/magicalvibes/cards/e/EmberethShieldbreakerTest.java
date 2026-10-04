package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BattleDisplay;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberethShieldbreaker.class, BattleDisplay.class, GoldenEgg.class, Plains.class})
class EmberethShieldbreakerTest extends BaseCardTest {

    @Test
    void adventureDestroysTargetArtifactAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());
        EmberethShieldbreaker card = new EmberethShieldbreaker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Golden Egg");
        harness.assertInGraveyard(player2, "Golden Egg");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetNonArtifactPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new EmberethShieldbreaker()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());
        EmberethShieldbreaker card = new EmberethShieldbreaker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Embereth Shieldbreaker");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void creatureCanBeCastDirectlyWithoutAnArtifactTarget() {
        harness.castFromHand(player1, new EmberethShieldbreaker(), "{1}{R}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Embereth Shieldbreaker");
        harness.assertNotInGraveyard(player1, "Embereth Shieldbreaker");
    }

    @Test
    void adventureCanDestroyAnArtifactControlledByItsCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GoldenEgg());
        EmberethShieldbreaker card = new EmberethShieldbreaker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Golden Egg");
        harness.assertInGraveyard(player1, "Golden Egg");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureGoesToGraveyardWithoutCastPermissionWhenItsTargetIsSacrificed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldenEgg());
        EmberethShieldbreaker card = new EmberethShieldbreaker();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, target.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Golden Egg");
        harness.assertInGraveyard(player1, "Embereth Shieldbreaker");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }
}
