package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GlacialRay;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.o.OrochiSustainer;
import com.github.laxika.magicalvibes.cards.s.SosukeSonOfSeshiro;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KashiTribeElite.class, SosukeSonOfSeshiro.class, OrochiSustainer.class,
        GlacialRay.class, KamiOfOldStone.class})
class KashiTribeEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary Snakes you control have shroud")
    void grantsShroudToLegendarySnakesYouControl() {
        Permanent kashi = addCreatureReady(player1, new KashiTribeElite());
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent ordinarySnake = addCreatureReady(player1, new OrochiSustainer());
        Permanent opposingSosuke = addCreatureReady(player2, new SosukeSonOfSeshiro());

        assertThat(gqs.hasKeyword(gd, sosuke, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, kashi, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, ordinarySnake, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingSosuke, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents targeting a legendary Snake")
    void cannotBeTargetedBySpells() {
        addCreatureReady(player1, new KashiTribeElite());
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        harness.setHand(player1, List.of(new GlacialRay()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, sosuke.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Combat damage to a creature taps it and locks its next untap step")
    void combatDamageTapsAndLocksDamagedCreature() {
        Permanent kashi = addCreatureReady(player1, new KashiTribeElite());
        kashi.setAttacking(true);
        addCreatureReady(player2, new KamiOfOldStone());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        Permanent kami = findPermanent(player2, "Kami of Old Stone");
        assertThat(kami.isTapped()).isTrue();
        assertThat(kami.getSkipUntapCount()).isEqualTo(1);
    }
}
