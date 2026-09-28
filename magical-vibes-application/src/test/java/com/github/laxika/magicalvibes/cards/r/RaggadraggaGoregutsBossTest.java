package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScaledWurm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaggadraggaGoregutsBoss.class, ElvishMystic.class, GrizzlyBears.class, ScaledWurm.class})
class RaggadraggaGoregutsBossTest extends BaseCardTest {

    @Test
    @DisplayName("Controlled creatures with mana abilities get +2/+2")
    void boostsControlledManaCreatures() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentMystic = addCreatureReady(player2, new ElvishMystic());

        assertThat(gqs.getEffectivePower(gd, mystic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mystic)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentMystic)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentMystic)).isEqualTo(1);
    }

    @Test
    @DisplayName("A mana creature that attacks is untapped")
    void attackingManaCreatureIsUntapped() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent mystic = addCreatureReady(player1, new ElvishMystic());

        declareAttackers(List.of(1));
        mystic.tap();
        resolveAllTriggers();

        assertThat(mystic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a spell with seven mana spent untaps, boosts, and gives trample to a target creature")
    void sevenManaSpellTriggersTargetedAbility() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new ScaledWurm()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Casting a spell with less than seven mana spent does not trigger the targeted ability")
    void fewerThanSevenManaDoesNotTrigger() {
        harness.addToBattlefield(player1, new RaggadraggaGoregutsBoss());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

}
