package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
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

class HexgoldHoverwingsTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Hexgold Hoverwings creates and equips a 2/2 Rebel token with flying")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new HexgoldHoverwings()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hoverwings = findPermanent(player1, "Hexgold Hoverwings");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(hoverwings.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Hoverwings boosts every equipped creature you control")
    void boostsEveryEquippedCreatureYouControl() {
        Permanent hoverwings = addHoverwingsReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent saw = new Permanent(new BoneSaw());
        saw.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(saw);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(hoverwings.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip moves Hoverwings and its abilities to another creature")
    void equipMovesHoverwings() {
        Permanent hoverwings = addHoverwingsReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(hoverwings.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    private Permanent addHoverwingsReady(Player player) {
        Permanent permanent = new Permanent(new HexgoldHoverwings());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
