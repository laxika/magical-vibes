package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castInstant(player2, 0, homura.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent essence = findPermanent(player1, "Homura's Essence");
        assertThat(essence.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Homura, Human Ascendant"));
    }

    @Test
    @DisplayName("Homura's Essence boosts and gives flying to creatures you control")
    void essenceBoostsOwnCreaturesAndGrantsFlying() {
        addEssence();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Homura's Essence gives creatures you control a red pump ability")
    void essenceGrantsRedPumpAbility() {
        addEssence();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bears), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    private Permanent addEssence() {
        Permanent essence = harness.addToBattlefieldAndReturn(player1, new HomuraHumanAscendant());
        essence.setTransformed(true);
        essence.setCard(essence.getOriginalCard().getBackFaceCard());
        return essence;
    }
}
