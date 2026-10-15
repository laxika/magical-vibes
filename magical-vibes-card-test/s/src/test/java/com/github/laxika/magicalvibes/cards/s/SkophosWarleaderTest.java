package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornBrute;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkophosWarleader.class, GrizzlyBears.class, GloriousAnthem.class, NyxbornBrute.class, OneWithTheStars.class})
class SkophosWarleaderTest extends BaseCardTest {

    @Test
    void sacrificesAnotherCreatureAndGainsPowerAndMenace() {
        Permanent warleader = addReadyWarlord();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId(), enchantment.getId());
        assertThat(choice.validIds()).doesNotContain(warleader.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(warleader.getPowerModifier()).isEqualTo(1);
        assertThat(warleader.getToughnessModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void sacrificesAnEnchantment() {
        Permanent warleader = addReadyWarlord();
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Glorious Anthem");
        assertThat(warleader.getPowerModifier()).isEqualTo(1);
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void boostAndMenaceWearOffAtEndOfTurn() {
        Permanent warleader = addReadyWarlord();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(warleader.getPowerModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isFalse();
    }

    @Test
    void cannotActivateWithoutAnotherCreatureOrEnchantment() {
        Permanent warleader = addReadyWarlord();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(warleader), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void canSacrificeItselfWhenItIsAnEnchantment() {
        Permanent warleader = harness.addToBattlefieldAndReturn(player1, new SkophosWarleader());
        harness.addToBattlefield(player1, new NyxbornBrute());
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, warleader.getId());
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, warleader)).isTrue();
        assertThat(gqs.isCreature(gd, warleader)).isFalse();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(warleader.getId());
        harness.handlePermanentChosen(player1, warleader.getId());

        harness.assertInGraveyard(player1, "Skophos Warleader");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Skophos Warleader");
        harness.assertOnBattlefield(player1, "Nyxborn Brute");
    }

    @Test
    void canActivateWhileSummoningSickAndSacrificeIsPaidBeforeResolution() {
        Permanent warleader = harness.addToBattlefieldAndReturn(player1, new SkophosWarleader());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new NyxbornBrute());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        harness.assertInGraveyard(player1, "Nyxborn Brute");
        assertThat(gd.stack).hasSize(1);
        assertThat(warleader.getPowerModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isFalse();

        harness.passBothPriorities();

        assertThat(warleader.getPowerModifier()).isEqualTo(1);
        assertThat(warleader.getToughnessModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isTrue();
    }

    @Test
    void repeatedActivationsAddPowerAndExpireTogether() {
        Permanent warleader = harness.addToBattlefieldAndReturn(player1, new SkophosWarleader());
        Permanent firstSacrifice = harness.addToBattlefieldAndReturn(player1, new NyxbornBrute());
        Permanent secondSacrifice = harness.addToBattlefieldAndReturn(player1, new NyxbornBrute());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);
        harness.handlePermanentChosen(player1, firstSacrifice.getId());
        harness.activateAbility(player1, battlefieldIndex(warleader), null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstSacrifice, secondSacrifice);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(warleader.getPowerModifier()).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(warleader.getPowerModifier()).isEqualTo(2);
        assertThat(warleader.getToughnessModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(warleader.getPowerModifier()).isZero();
        assertThat(warleader.hasKeyword(Keyword.MENACE)).isFalse();
    }

    private Permanent addReadyWarlord() {
        return addCreatureReady(player1, new SkophosWarleader());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
