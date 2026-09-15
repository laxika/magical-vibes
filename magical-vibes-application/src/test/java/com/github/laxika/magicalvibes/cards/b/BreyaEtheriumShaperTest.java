package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({BreyaEtheriumShaper.class, GrizzlyBears.class})
class BreyaEtheriumShaperTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two blue 1/1 Thopter artifact creature tokens with flying")
    void etbCreatesTwoThopters() {
        addBreya();

        assertThat(findPermanents(player1, "Thopter")).hasSize(2);
        assertThat(findPermanents(player1, "Thopter")).allSatisfy(thopter -> {
            assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(1);
            assertThat(thopter.getCard().getColors()).containsExactly(CardColor.BLUE);
            assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
            assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        });
    }

    @Test
    @DisplayName("The damage mode sacrifices two artifacts and deals 3 damage to a player")
    void damageModeSacrificesArtifactsAndDamagesPlayer() {
        addBreya();
        Permanent firstThopter = findPermanents(player1, "Thopter").get(0);
        Permanent secondThopter = findPermanents(player1, "Thopter").get(1);
        int lifeBefore = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.handlePermanentChosen(player1, firstThopter.getId());
        harness.handlePermanentChosen(player1, secondThopter.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("The debuff mode gives a target creature -4/-4 until end of turn")
    void debuffModeKillsTargetCreature() {
        addBreya();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.handlePermanentChosen(player1, thopters.get(0).getId());
        harness.handlePermanentChosen(player1, thopters.get(1).getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The life-gain mode sacrifices two artifacts and gains 5 life")
    void lifeGainModeGainsFiveLife() {
        addBreya();
        List<Permanent> thopters = findPermanents(player1, "Thopter");
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.handlePermanentChosen(player1, thopters.get(0).getId());
        harness.handlePermanentChosen(player1, thopters.get(1).getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    void damageModeRejectsCreatureTarget() {
        addBreya();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addBreya() {
        Permanent breya = harness.enterBattlefieldAndReturn(player1, new BreyaEtheriumShaper());
        harness.passBothPriorities();
        return breya;
    }
}
