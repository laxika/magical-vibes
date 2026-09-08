package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Guardian Idol enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new GuardianIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Guardian Idol").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping Guardian Idol produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent idol = addIdolReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(idol.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Guardian Idol becomes a 2/2 Golem artifact creature")
    void animationMakesCreature() {
        Permanent idol = addIdolReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, idol)).isTrue();
        assertThat(gqs.isArtifact(idol)).isTrue();
        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(2);
        assertThat(idol.getTransientSubtypes()).containsExactly(CardSubtype.GOLEM);
    }

    @Test
    @DisplayName("Guardian Idol stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent idol = addIdolReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, idol)).isFalse();
        assertThat(idol.getTransientSubtypes()).isEmpty();
    }

    private Permanent addIdolReady(Player player) {
        Permanent permanent = new Permanent(new GuardianIdol());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
