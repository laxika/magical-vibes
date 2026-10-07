package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.c.CapsizingWave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwordCoastSerpent.class, CapsizingWave.class, GrizzlyBears.class, DarkRitual.class, Island.class})
class SwordCoastSerpentTest extends BaseCardTest {

    @Test
    void adventureReturnsTargetCreatureAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SwordCoastSerpent card = new SwordCoastSerpent();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(harness.getGameData().playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(harness.getGameData().findExiledCard(card.getId())).isNotNull();
        assertThat(harness.getGameData().exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        SwordCoastSerpent card = new SwordCoastSerpent();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureCannotBeBlockedOnlyAfterCastingANoncreatureSpell() {
        Permanent serpent = addCreatureReady(player1, new SwordCoastSerpent());
        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isFalse();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isFalse();

        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isTrue();
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        SwordCoastSerpent card = new SwordCoastSerpent();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Sword Coast Serpent");
        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isTrue();
    }

    @Test
    void opponentsAdventureDoesNotMakeSerpentUnblockable() {
        Permanent serpent = addCreatureReady(player1, new SwordCoastSerpent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordCoastSerpent());
        harness.setHand(player2, List.of(new SwordCoastSerpent()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);

        harness.castAdventure(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isFalse();
    }

    @Test
    void adventureCountsImmediatelyAndUnblockabilityExpiresNextTurn() {
        Permanent serpent = addCreatureReady(player1, new SwordCoastSerpent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordCoastSerpent());
        harness.setHand(player1, List.of(new SwordCoastSerpent()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAdventure(player1, 0, target.getId());
        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isTrue();
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isFalse();
    }

    @Test
    void adventureWithMissingTargetGoesToGraveyardButStillCountsAsCast() {
        Permanent serpent = addCreatureReady(player1, new SwordCoastSerpent());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordCoastSerpent());
        SwordCoastSerpent first = new SwordCoastSerpent();
        SwordCoastSerpent response = new SwordCoastSerpent();
        harness.setHand(player1, List.of(first, response));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAdventure(player1, 0, target.getId());
        harness.ensurePriority(player1);
        harness.castAdventure(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(first.getId());
        assertThat(gd.findExiledCard(response.getId())).isNotNull();
        assertThat(gqs.hasCantBeBlocked(gd, serpent)).isTrue();
    }

}
