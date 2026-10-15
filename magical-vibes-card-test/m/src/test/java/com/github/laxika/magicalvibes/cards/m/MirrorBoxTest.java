package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.e.EiganjoSeatOfTheEmpire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.n.NorikaYamazakiThePoet;
import com.github.laxika.magicalvibes.cards.t.TsaboTavoc;
import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirrorBox.class, GrizzlyBears.class, TsaboTavoc.class, JukaiTrainee.class,
        NorikaYamazakiThePoet.class, EiganjoSeatOfTheEmpire.class, WitnessProtection.class, DressDown.class})
class MirrorBoxTest extends BaseCardTest {

    @Test
    @DisplayName("Duplicate legendary permanents survive and legendary creatures get +1/+1")
    void ignoresLegendRuleAndBoostsLegendaryCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        int basePower = gqs.getEffectivePower(gd, first);
        int baseToughness = gqs.getEffectiveToughness(gd, first);

        Permanent box = harness.addToBattlefieldAndReturn(player1, new MirrorBox());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TsaboTavoc());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, box, second);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(baseToughness + 2);
    }

    @Test
    @DisplayName("Only nontoken creatures get same-name bonuses, while tokens still count")
    void boostsNontokenCreaturesByOtherControlledCreaturesWithTheSameName() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int basePower = gqs.getEffectivePower(gd, first);
        int baseToughness = gqs.getEffectiveToughness(gd, first);
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);

        harness.addToBattlefield(player1, new MirrorBox());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("The legend-rule exemption does not protect permanents an opponent controls")
    void onlyExemptsPermanentsControlledByItsController() {
        harness.addToBattlefield(player1, new MirrorBox());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new TsaboTavoc());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new TsaboTavoc());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    void legendaryNoncreaturesSurviveUntilMirrorBoxLeaves() {
        Permanent box = harness.addToBattlefieldAndReturn(player1, new MirrorBox());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new EiganjoSeatOfTheEmpire());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new EiganjoSeatOfTheEmpire());

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(box, first, second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(box);
        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }

    @Test
    void legendaryTokensReceiveOnlyTheLegendaryBonusAndIncreaseNontokenBonus() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new NorikaYamazakiThePoet());
        int basePower = gqs.getEffectivePower(gd, creature);
        int baseToughness = gqs.getEffectiveToughness(gd, creature);
        Card tokenCard = new NorikaYamazakiThePoet();
        tokenCard.setToken(true);
        harness.addToBattlefield(player1, new MirrorBox());
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new NorikaYamazakiThePoet());

        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(baseToughness + 2);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(baseToughness + 1);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(baseToughness);
    }

    @Test
    void changingOneCreaturesNameStopsTheSameNameBonus() {
        harness.addToBattlefield(player1, new MirrorBox());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new WitnessProtection());
        aura.setAttachedTo(second.getId());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(1);
    }

    @Test
    void faceDownCreaturesHaveNoMatchingName() {
        harness.addToBattlefield(player1, new MirrorBox());
        Permanent faceUp = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        first.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        second.setFaceDown(2, 2, Set.of(CardType.CREATURE));

        assertThat(gqs.getEffectivePower(gd, faceUp)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, faceUp)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void losingCreatureAbilitiesDoesNotRemoveMirrorBoxsSameNameBonus() {
        harness.addToBattlefield(player1, new MirrorBox());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        harness.addToBattlefield(player2, new DressDown());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void faceDownMirrorBoxDoesNotExemptLegends() {
        Permanent box = harness.addToBattlefieldAndReturn(player1, new MirrorBox());
        box.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NorikaYamazakiThePoet());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NorikaYamazakiThePoet());

        harness.runStateBasedActions();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(first.getId(), second.getId());
    }
}
