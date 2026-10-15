package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.v.VirulentSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SliverLegion.class, VirulentSliver.class, BlindPhantasm.class,
        ArtificialEvolution.class, Bitterblossom.class})
class SliverLegionTest extends BaseCardTest {

    @Test
    void loneLegionDoesNotCountItself() {
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new SliverLegion());

        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(7);
    }

    @Test
    void legionsControlledByDifferentPlayersHaveCumulativeBonuses() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SliverLegion());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SliverLegion());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new VirulentSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(11);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(11);
        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(5);
    }

    @Test
    void bonusDisappearsWhenLegionLeaves() {
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new SliverLegion());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new VirulentSliver());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new VirulentSliver());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(legion);

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    @CardUsed({ArtificialEvolution.class, Bitterblossom.class})
    void countsNoncreatureKindredSlivers() {
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new SliverLegion());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new Bitterblossom());

        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SLIVER");

        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(8);
    }

    @Test
    @DisplayName("Sliver creatures get +1/+1 for each other Sliver on the battlefield")
    void boostsSliversByOtherSlivers() {
        Permanent firstSliver = harness.addToBattlefieldAndReturn(player1, new VirulentSliver());
        Permanent secondSliver = harness.addToBattlefieldAndReturn(player2, new VirulentSliver());
        Permanent unrelatedCreature = harness.addToBattlefieldAndReturn(player2, new BlindPhantasm());
        int firstBasePower = firstSliver.getEffectivePower();
        int firstBaseToughness = firstSliver.getEffectiveToughness();
        int secondBasePower = secondSliver.getEffectivePower();
        int secondBaseToughness = secondSliver.getEffectiveToughness();
        int unrelatedBasePower = unrelatedCreature.getEffectivePower();
        int unrelatedBaseToughness = unrelatedCreature.getEffectiveToughness();
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new SliverLegion());
        int legionBasePower = legion.getCard().getPower();
        int legionBaseToughness = legion.getCard().getToughness();

        assertThat(gqs.getEffectivePower(gd, firstSliver)).isEqualTo(firstBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, firstSliver)).isEqualTo(firstBaseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, secondSliver)).isEqualTo(secondBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, secondSliver)).isEqualTo(secondBaseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(legionBasePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(legionBaseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, unrelatedCreature)).isEqualTo(unrelatedBasePower);
        assertThat(gqs.getEffectiveToughness(gd, unrelatedCreature)).isEqualTo(unrelatedBaseToughness);
    }

    @Test
    @DisplayName("The bonus updates when another Sliver leaves the battlefield")
    void bonusUpdatesWhenSliverLeaves() {
        Permanent legion = harness.addToBattlefieldAndReturn(player1, new SliverLegion());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new VirulentSliver());
        int legionBasePower = legion.getCard().getPower();
        int legionBaseToughness = legion.getCard().getToughness();
        int sliverBasePower = sliver.getCard().getPower();
        int sliverBaseToughness = sliver.getCard().getToughness();

        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(legionBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(legionBaseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, sliver)).isEqualTo(sliverBasePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, sliver)).isEqualTo(sliverBaseToughness + 1);

        gd.playerBattlefields.get(player1.getId()).remove(sliver);

        assertThat(gqs.getEffectivePower(gd, legion)).isEqualTo(legionBasePower);
        assertThat(gqs.getEffectiveToughness(gd, legion)).isEqualTo(legionBaseToughness);
    }
}
