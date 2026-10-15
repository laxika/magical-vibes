package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArdenvaleTacticianDizzyingSwoop.class, YouthfulKnight.class})
class ArdenvaleTacticianDizzyingSwoopTest extends BaseCardTest {

    @Test
    void adventureTapsUpToTwoCreaturesAndExilesTheCardWithPermissionToCastTheCreatureFace() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        ArdenvaleTacticianDizzyingSwoop card = new ArdenvaleTacticianDizzyingSwoop();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).hasSize(3);
        harness.assertOnBattlefield(player1, "Ardenvale Tactician");
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNull();
    }

    @Test
    void adventureCanResolveWithNoTargetsWithoutTappingAnyCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        ArdenvaleTacticianDizzyingSwoop card = prepareAdventure();

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
        harness.assertNotInGraveyard(player1, "Ardenvale Tactician");
    }

    @Test
    void adventureCanTapOneOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        ArdenvaleTacticianDizzyingSwoop card = prepareAdventure();

        harness.castAdventure(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureStillResolvesWhenOnlyOneOfTwoTargetsLeavesTheBattlefield() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        ArdenvaleTacticianDizzyingSwoop card = prepareAdventure();

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureGoesToGraveyardWithoutCastPermissionWhenAllTargetsLeave() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new YouthfulKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        ArdenvaleTacticianDizzyingSwoop card = prepareAdventure();

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerBattlefields.get(player2.getId()).remove(second);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ardenvale Tactician");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureCannotTargetMoreThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        prepareAdventure();

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void adventureCannotTargetTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YouthfulKnight());
        prepareAdventure();

        assertThatThrownBy(() -> harness.castAdventure(player1, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureFaceCanBeCastDirectlyWithoutGoingOnAdventure() {
        harness.setHand(player1, List.of(new ArdenvaleTacticianDizzyingSwoop()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ardenvale Tactician");
        harness.assertNotInGraveyard(player1, "Ardenvale Tactician");
    }

    private ArdenvaleTacticianDizzyingSwoop prepareAdventure() {
        ArdenvaleTacticianDizzyingSwoop card = new ArdenvaleTacticianDizzyingSwoop();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        return card;
    }
}
