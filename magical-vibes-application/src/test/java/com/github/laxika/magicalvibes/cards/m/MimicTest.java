package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Mimic.class)
class MimicTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing Mimic adds one mana of the chosen color")
    void tappingAndSacrificingAddsChosenMana() {
        Permanent mimic = addMimicReady();

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        assertThat(gameData.playerBattlefields.get(player1.getId())).doesNotContain(mimic);

        harness.handleListChoice(player1, "GREEN");

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(mimic.getCard());
    }

    @Test
    @DisplayName("Paying two mana makes Mimic a 3/3 Shapeshifter artifact creature")
    void payingTwoManaAnimatesMimic() {
        Permanent mimic = addMimicReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mimic)).isTrue();
        assertThat(gqs.isArtifact(mimic)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mimic)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mimic)).isEqualTo(3);
        assertThat(mimic.getTransientSubtypes()).contains(CardSubtype.SHAPESHIFTER);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mimic stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent mimic = addMimicReady();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mimic)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mimic)).isFalse();
        assertThat(gqs.isArtifact(mimic)).isTrue();
        assertThat(mimic.getTransientSubtypes()).doesNotContain(CardSubtype.SHAPESHIFTER);
    }

    private Permanent addMimicReady() {
        Permanent mimic = harness.addToBattlefieldAndReturn(player1, new Mimic());
        mimic.setSummoningSick(false);
        return mimic;
    }
}
