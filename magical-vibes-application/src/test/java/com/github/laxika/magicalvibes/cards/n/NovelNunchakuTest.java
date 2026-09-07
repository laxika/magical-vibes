package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({NovelNunchaku.class, GrizzlyBears.class, HillGiant.class})
class NovelNunchakuTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached and its reflexive ability fights an opponent's creature")
    void entersAttachedAndFights() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent nunchaku = findPermanent(player1, "Novel Nunchaku");
        assertThat(nunchaku.getAttachedTo()).isEqualTo(giant.getId());
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, giant, Keyword.TRAMPLE)).isTrue();
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The reflexive fight may choose no target")
    void mayChooseNoFightTarget() {
        Permanent giant = addCreatureReady(player1, new HillGiant());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castNunchaku(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The enter ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NovelNunchaku()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip attaches Novel Nunchaku to a creature you control")
    void equipAttachesToCreature() {
        Permanent nunchaku = addNunchakuReady();
        Permanent giant = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(nunchaku.getAttachedTo()).isEqualTo(giant.getId());
    }

    private void castNunchaku(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new NovelNunchaku()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0, targetId);
    }

    private Permanent addNunchakuReady() {
        Permanent nunchaku = new Permanent(new NovelNunchaku());
        nunchaku.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(nunchaku);
        return nunchaku;
    }
}
