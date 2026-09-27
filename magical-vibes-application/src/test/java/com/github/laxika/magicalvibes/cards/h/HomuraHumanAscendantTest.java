package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.k.KikusShadow;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HomuraHumanAscendant.class, HomurasEssence.class, HandOfHonor.class, KikusShadow.class})
class HomuraHumanAscendantTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent homura = addCreatureReady(player1, new HomuraHumanAscendant());

        assertThat(bls.canBlock(gd, homura)).isFalse();
    }

    @Test
    @DisplayName("Returns to the battlefield transformed when it dies")
    void returnsTransformedOnDeath() {
        Permanent homura = harness.addToBattlefieldAndReturn(player1, new HomuraHumanAscendant());
        harness.setHand(player1, List.of(new KikusShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, homura.getId());
        harness.passBothPriorities();

        Permanent essence = findPermanent(player1, "Homura's Essence");
        assertThat(essence.isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Homura, Human Ascendant");
    }

    @Test
    @DisplayName("Homura's Essence boosts and gives flying to creatures you control")
    void essenceBoostsOwnCreaturesAndGrantsFlying() {
        addEssence();
        Permanent honor = addCreatureReady(player1, new HandOfHonor());

        assertThat(gqs.getEffectivePower(gd, honor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, honor)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, honor, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Homura's Essence affects only creatures you control")
    void essenceAffectsOnlyOwnCreatures() {
        addEssence();
        Permanent ownCreature = addCreatureReady(player1, new HandOfHonor());
        Permanent opposingCreature = addCreatureReady(player2, new HandOfHonor());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();

        harness.addMana(player2, ManaColor.RED, 1);
        int opposingCreatureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opposingCreature);
        assertThatThrownBy(() -> harness.activateAbility(player2, opposingCreatureIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Homura's Essence gives creatures you control a red pump ability")
    void essenceGrantsRedPumpAbility() {
        addEssence();
        Permanent honor = addCreatureReady(player1, new HandOfHonor());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(honor), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, honor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, honor)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, honor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, honor)).isEqualTo(4);
    }

    private Permanent addEssence() {
        Permanent essence = harness.addToBattlefieldAndReturn(player1, new HomuraHumanAscendant());
        essence.setTransformed(true);
        essence.setCard(essence.getOriginalCard().getBackFaceCard());
        return essence;
    }
}
