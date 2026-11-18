package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieGuidemother.class, GrizzlyBears.class})
class FaerieGuidemotherTest extends BaseCardTest {

    @Test
    void adventureBoostsTargetCreatureAndGrantsFlyingUntilEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        FaerieGuidemother card = new FaerieGuidemother();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isEqualTo(1);
        assertThat(bear.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissionHolder(card.getId())).isEqualTo(player1.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, bear)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, bear)).isZero();
        assertThat(bear.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FaerieGuidemother());
        FaerieGuidemother card = new FaerieGuidemother();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(2);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(1);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(card.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardWithoutExilePermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FaerieGuidemother());
        FaerieGuidemother card = new FaerieGuidemother();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAdventure(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(card.getId());
    }

    @Test
    void creatureCanBeCastDirectlyWithoutGoingOnAdventure() {
        FaerieGuidemother card = new FaerieGuidemother();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(card.getId());
    }
}
