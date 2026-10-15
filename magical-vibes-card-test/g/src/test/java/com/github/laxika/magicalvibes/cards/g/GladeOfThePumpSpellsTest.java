package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CrucibleOfWorlds;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GladeOfThePumpSpells.class, GrizzlyBears.class, CrucibleOfWorlds.class})
class GladeOfThePumpSpellsTest extends BaseCardTest {

    @Test
    void playingItPaysItsAdditionalLandCostAndPumpsAChosenCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GladeOfThePumpSpells()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotPlayItWithoutItsManaCost() {
        harness.setHand(player1, List.of(new GladeOfThePumpSpells()));

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapsForTwoGreenMana() {
        Permanent glade = harness.addToBattlefieldAndReturn(player1, new GladeOfThePumpSpells());
        glade.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(glade.isTapped()).isTrue();
    }

    @Test
    void canDeclineToTargetAnAvailableCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GladeOfThePumpSpells()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glade of the Pump Spells");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canPumpOpponentsCreatureAndBothBonusesExpire() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GladeOfThePumpSpells()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void playingFromGraveyardStillRequiresMana() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new GladeOfThePumpSpells()));

        assertThatThrownBy(() -> harness.playLandFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Glade of the Pump Spells");
        harness.assertNotOnBattlefield(player1, "Glade of the Pump Spells");
    }

    @Test
    void playingFromGraveyardConsumesMana() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GladeOfThePumpSpells()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.playLandFromGraveyard(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glade of the Pump Spells");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void genericManaCannotReplaceTheRequiredGreenMana() {
        harness.setHand(player1, List.of(new GladeOfThePumpSpells()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Glade of the Pump Spells");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
