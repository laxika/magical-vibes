package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SylvokBattleChairTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Sylvok Battle-Chair creates and equips a 2/2 Rebel token")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new SylvokBattleChair()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent chair = findPermanent(player1, "Sylvok Battle-Chair");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(chair.getAttachedTo()).isEqualTo(rebel.getId());
    }

    @Test
    @DisplayName("Sylvok Battle-Chair gives the equipped creature +4/+4 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent chair = addChairReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        chair.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipping Sylvok Battle-Chair moves it to another creature")
    void equipMovesChairToAnotherCreature() {
        Permanent chair = addChairReady(player1);
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        chair.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(chair.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addChairReady(Player player) {
        Permanent permanent = new Permanent(new SylvokBattleChair());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
