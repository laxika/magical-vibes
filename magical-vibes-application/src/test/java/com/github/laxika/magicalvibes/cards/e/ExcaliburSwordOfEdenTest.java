package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExcaliburSwordOfEden.class, GrizzlyBears.class, IsamaruHoundOfKonda.class,
        JhoirasFamiliar.class, HistoryOfBenalia.class})
class ExcaliburSwordOfEdenTest extends BaseCardTest {

    @Test
    @DisplayName("Excalibur costs less by the total mana value of historic permanents you control")
    void costReductionUsesTotalHistoricManaValue() {
        harness.addToBattlefield(player1, new JhoirasFamiliar());
        harness.addToBattlefield(player1, new IsamaruHoundOfKonda());
        harness.addToBattlefield(player1, new HistoryOfBenalia());
        harness.setHand(player1, List.of(new ExcaliburSwordOfEden()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Nonhistoric permanents do not reduce Excalibur's cost")
    void nonHistoricPermanentsDoNotReduceCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExcaliburSwordOfEden()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipped creature gets +10/+0 and vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburSwordOfEden());
        excalibur.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equip {2} can target a legendary creature you control")
    void equipTargetsLegendaryCreature() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburSwordOfEden());
        Permanent legendaryCreature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(excalibur),
                0, null, legendaryCreature.getId());
        harness.passBothPriorities();

        assertThat(excalibur.getAttachedTo()).isEqualTo(legendaryCreature.getId());
    }

    @Test
    @DisplayName("Equip {2} rejects a nonlegendary creature")
    void equipRejectsNonlegendaryCreature() {
        Permanent excalibur = harness.addToBattlefieldAndReturn(player1, new ExcaliburSwordOfEden());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(excalibur),
                0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }
}
