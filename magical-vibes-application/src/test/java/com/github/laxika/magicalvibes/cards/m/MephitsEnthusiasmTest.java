package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MephitsEnthusiasm.class, GrizzlyBears.class, SerraAngel.class, ChandraNalaar.class})
class MephitsEnthusiasmTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetCreatureWithoutExcessDamageBoon() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, bears))).isEqualTo(2);
    }

    @Test
    void excessDamageGivesTheNextCreatureSpellTheNotedPerpetualPowerBoostOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAt(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, firstBears))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, findPermanentByCard(player1, secondBears))).isEqualTo(2);
    }

    @Test
    void dealsFourDamageToTargetPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 6);
        castAt(planeswalker);

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new MephitsEnthusiasm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private Permanent findPermanentByCard(com.github.laxika.magicalvibes.model.Player player, Card card) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
