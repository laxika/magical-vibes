package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HondenOfCleansingFire;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WearAway.class, SenseisDiviningTop.class, HondenOfCleansingFire.class,
        WanderingOnes.class, YamabushisFlame.class})
class WearAwayTest extends BaseCardTest {

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new WearAway()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new SenseisDiviningTop());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Sensei's Divining Top");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Sensei's Divining Top");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new HondenOfCleansingFire());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Honden of Cleansing Fire");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertInGraveyard(player2, "Honden of Cleansing Fire");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new SenseisDiviningTop());
        harness.addToBattlefield(player2, new WanderingOnes());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Wandering Ones");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        harness.addToBattlefield(player2, new SenseisDiviningTop());
        WearAway host = new WearAway();
        WearAway spliced = new WearAway();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(host, spliced));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID targetId = harness.getPermanentId(player2, "Sensei's Divining Top");
        gs.playCardWithSplice(gd, player1, 0, 0, null, null,
                List.of(targetId, targetId), List.of(1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sensei's Divining Top");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spliced);
    }

    @Test
    @DisplayName("Cannot splice onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        YamabushisFlame host = new YamabushisFlame();
        WearAway spliced = new WearAway();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(host, spliced));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(host, spliced);
    }
}
