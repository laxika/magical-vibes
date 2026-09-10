package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AllureOfPower;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyPrecious.class, AllureOfPower.class, GrizzlyBears.class, Forest.class, Mountain.class})
class MyPreciousTest extends BaseCardTest {

    @Test
    void equippedCreatureHasHexproofAndCannotBeBlocked() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    void equipPaysLifeAndAttachesToTargetCreature() {
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new MyPrecious());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(equipment), null,
                creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void adventureSacrificesCreatureDrawsTwoAndExilesTheCard() {
        MyPrecious card = new MyPrecious();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(forest, mountain));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, sacrifice.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void adventureCannotBeCastWithoutAChosenCreature() {
        MyPrecious card = new MyPrecious();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, null, List.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
