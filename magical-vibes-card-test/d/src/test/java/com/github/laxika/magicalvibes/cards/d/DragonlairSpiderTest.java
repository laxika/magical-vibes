package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FracturedPowerstone;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonlairSpider.class, GrizzlyBears.class, Forest.class, FracturedPowerstone.class})
class DragonlairSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's spell creates a 1/1 green Insect token")
    void opponentSpellCreatesInsect() {
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        Permanent insect = findPermanent(player1, "Insect");
        assertThat(insect).isNotNull();
        assertThat(gqs.getEffectivePower(gd, insect)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, insect)).isEqualTo(1);
    }

    @Test
    @DisplayName("Your own spell does not create an Insect token")
    void ownSpellDoesNotCreateInsect() {
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    @DisplayName("A noncreature spell creates its Insect before the spell resolves")
    void noncreatureSpellCreatesInsectBeforeResolving() {
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FracturedPowerstone()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);

        assertThat(countPermanents(player1, "Insect")).isZero();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        Permanent insect = findPermanent(player1, "Insect");
        assertThat(insect.getCard().isToken()).isTrue();
        assertThat(insect.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(insect.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.getEffectivePower(gd, insect)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, insect)).isEqualTo(1);
        assertThat(countPermanents(player2, "Insect")).isZero();
        assertThat(countPermanents(player2, "Fractured Powerstone")).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(countPermanents(player2, "Fractured Powerstone")).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Spider triggers for every opposing spell")
    void multipleSpidersTriggerForEachSpell() {
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FracturedPowerstone(), new FracturedPowerstone()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Insect")).isEqualTo(4);
        assertThat(countPermanents(player2, "Insect")).isZero();
    }

    @Test
    @DisplayName("A triggered ability still creates its Insect after the Spider leaves")
    void triggerSurvivesSourceLeavingBattlefield() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new DragonlairSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FracturedPowerstone()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        gd.playerBattlefields.get(player1.getId()).remove(spider);
        gd.playerGraveyards.get(player1.getId()).add(spider.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
        assertThat(countPermanents(player2, "Insect")).isZero();
    }

    @Test
    @DisplayName("An opponent playing a land does not trigger the Spider")
    void playingLandDoesNotCreateInsect() {
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Forest")).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isZero();
    }
}
