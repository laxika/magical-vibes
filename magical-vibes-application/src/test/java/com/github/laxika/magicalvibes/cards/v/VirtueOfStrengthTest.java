package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarenbrigGrowth;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VirtueOfStrength.class, GarenbrigGrowth.class, Forest.class, GrizzlyBears.class,
        Glimmerpost.class, LlanowarElves.class})
class VirtueOfStrengthTest extends BaseCardTest {

    @Test
    void adventureReturnsTargetCreatureToHandAndExilesCard() {
        Card creature = new GrizzlyBears();
        VirtueOfStrength card = new VirtueOfStrength();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureReturnsTargetLandToHand() {
        Card land = new Forest();
        VirtueOfStrength card = new VirtueOfStrength();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
    }

    @Test
    void adventureCannotTargetAnEnchantmentCard() {
        Card enchantment = new VirtueOfStrength();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new VirtueOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentFaceCanBeCastFromExileAfterAdventure() {
        Card land = new Forest();
        VirtueOfStrength card = new VirtueOfStrength();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAdventure(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(card.getId()));
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void enchantmentTriplesBasicLandManaButNotOtherPermanents() {
        harness.addToBattlefield(player1, new VirtueOfStrength());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent glimmerpost = harness.addToBattlefieldAndReturn(player1, new Glimmerpost());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(glimmerpost));
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
