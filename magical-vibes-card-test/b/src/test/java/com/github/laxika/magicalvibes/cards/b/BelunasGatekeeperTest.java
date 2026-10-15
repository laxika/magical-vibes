package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.e.EntryDenied;
import com.github.laxika.magicalvibes.cards.s.SnaremasterSprite;
import com.github.laxika.magicalvibes.cards.r.RedcapThief;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TuinvaleGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BelunasGatekeeper.class, EntryDenied.class, SnaremasterSprite.class, Island.class,
        TuinvaleGuide.class, RedcapThief.class})
class BelunasGatekeeperTest extends BaseCardTest {

    @Test
    void adventureReturnsEligibleCreatureAndExilesTheCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnaremasterSprite());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snaremaster Sprite");
        harness.assertInHand(player2, "Snaremaster Sprite");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetAnIneligiblePermanent() {
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player2, new TuinvaleGuide());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, largeCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value 3 or less");
    }

    @Test
    void adventureCannotTargetACreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SnaremasterSprite());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you don't control");
    }

    @Test
    void adventureCannotTargetALand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you don't control");
    }

    @Test
    void adventureCanReturnACreatureWithManaValueExactlyThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RedcapThief());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Redcap Thief");
        harness.assertInHand(player2, "Redcap Thief");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnaremasterSprite());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beluna's Gatekeeper");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void creatureCanBeCastFromHandWithoutGoingOnAdventure() {
        harness.castFromHand(player1, new BelunasGatekeeper(), "{5}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Beluna's Gatekeeper");
        harness.assertNotInGraveyard(player1, "Beluna's Gatekeeper");
    }

    @Test
    void adventureGoesToGraveyardWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnaremasterSprite());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureGoesToGraveyardWhenCasterGainsControlOfTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnaremasterSprite());
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Snaremaster Sprite");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId());
    }

    @Test
    void adventureReturnsCreatureToOwnerRatherThanController() {
        SnaremasterSprite creature = new SnaremasterSprite();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        BelunasGatekeeper card = new BelunasGatekeeper();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snaremaster Sprite");
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
