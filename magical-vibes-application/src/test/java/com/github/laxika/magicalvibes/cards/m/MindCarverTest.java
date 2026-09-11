package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindCarver.class, GrizzlyBears.class, Spellbook.class})
class MindCarverTest extends BaseCardTest {

    @Test
    @DisplayName("Mind Carver enters attached to a target creature you control")
    void entersAttachedToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindCarver()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mindCarver = findPermanent(player1, "Mind Carver");
        assertThat(mindCarver.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mind Carver gets the larger bonus when an opponent has eight graveyard cards")
    void getsLargerBonusAtGraveyardThreshold() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mindCarver = harness.addToBattlefieldAndReturn(player1, new MindCarver());
        mindCarver.setAttachedTo(bears.getId());

        setGraveyardSize(player2, 7);
        assertStats(bears, 3, 2);

        setGraveyardSize(player2, 8);
        assertStats(bears, 5, 3);

        gd.playerGraveyards.get(player2.getId()).removeFirst();
        assertStats(bears, 3, 2);
    }

    @Test
    @DisplayName("Mind Carver ignores the controller's graveyard for its larger bonus")
    void ownGraveyardDoesNotEnableLargerBonus() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent mindCarver = harness.addToBattlefieldAndReturn(player1, new MindCarver());
        mindCarver.setAttachedTo(bears.getId());
        setGraveyardSize(player1, 8);

        assertStats(bears, 3, 2);
    }

    @Test
    @DisplayName("Mind Carver cannot target an opponent's creature on entry")
    void cannotTargetOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MindCarver()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Equip attaches Mind Carver for two generic and one black mana")
    void equipAttachesToCreature() {
        Permanent mindCarver = new Permanent(new MindCarver());
        mindCarver.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mindCarver);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(mindCarver.getAttachedTo()).isEqualTo(bears.getId());
    }

    private void setGraveyardSize(Player player, int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Spellbook());
        }
        harness.setGraveyard(player, cards);
    }

    private void assertStats(Permanent creature, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(toughness);
    }
}
