package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.EnchantedRiversGrasp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoneFishing;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LakeTownMariners.class, GoneFishing.class, Forest.class, GrizzlyBears.class, EnchantedRiversGrasp.class})
class LakeTownMarinersTest extends BaseCardTest {

    @Test
    void adventureExilesAndReturnsCreatureAndLandUnderTheirOwnersControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        LakeTownMariners card = new LakeTownMariners();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).anyMatch(permanent -> permanent.getOriginalCard() == creature.getOriginalCard());
        assertThat(battlefield).anyMatch(permanent -> permanent.getOriginalCard() == land.getOriginalCard());
        assertThat(battlefield).noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(battlefield).noneMatch(permanent -> permanent.getId().equals(land.getId()));
        assertThat(gd.findExiledCard(card.getId()).card()).isSameAs(card);
    }

    @Test
    void adventureCannotTargetAnOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LakeTownMariners()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castAdventure(
                player1, 0, List.of(opponentCreature.getId(), ownLand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or land you control");
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        LakeTownMariners card = new LakeTownMariners();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void wardCountersOpponentsAuraWhenTheyCannotPay() {
        Permanent mariners = harness.addToBattlefieldAndReturn(player1, new LakeTownMariners());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new EnchantedRiversGrasp()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player2, 0, mariners.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Enchanted River's Grasp");
        harness.assertNotOnBattlefield(player2, "Enchanted River's Grasp");
        assertThat(mariners.isTapped()).isFalse();
    }

    @Test
    void vigilanceAllowsAttackingWithoutTapping() {
        Permanent mariners = addCreatureReady(player1, new LakeTownMariners());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(mariners.isTapped()).isFalse();
        harness.assertLife(player2, 14);
    }

    @Test
    void adventureCanFlickerTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LakeTownMariners());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LakeTownMariners());
        LakeTownMariners card = new LakeTownMariners();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .noneMatch(permanent -> permanent.getId().equals(first.getId())
                        || permanent.getId().equals(second.getId()));
        assertThat(gd.findExiledCard(card.getId()).card()).isSameAs(card);
    }

    @Test
    void adventureCanFlickerTwoLands() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new LakeTownMariners()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2)
                .allMatch(permanent -> !permanent.isTapped())
                .noneMatch(permanent -> permanent.getId().equals(first.getId())
                        || permanent.getId().equals(second.getId()));
    }

    @Test
    void adventureReturnsBorrowedCreatureToItsOwner() {
        LakeTownMariners borrowed = new LakeTownMariners();
        borrowed.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, borrowed);
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LakeTownMariners()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAdventure(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard() == borrowed);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == borrowed
                        && !permanent.getId().equals(creature.getId()));
    }

    @Test
    void adventureSkipsTargetThatAnOpponentNowControls() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LakeTownMariners());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        LakeTownMariners card = new LakeTownMariners();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard() == land.getOriginalCard()
                        && !permanent.getId().equals(land.getId()));
        assertThat(gd.findExiledCard(card.getId()).card()).isSameAs(card);
    }

    @Test
    void adventureWithNoLegalTargetsGoesToGraveyardInsteadOfExile() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        LakeTownMariners card = new LakeTownMariners();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(first, second));
        gd.playerBattlefields.get(player2.getId()).addAll(List.of(first, second));

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
    }

    @Test
    void adventureRequiresTwoDistinctTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new LakeTownMariners()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of(land.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
