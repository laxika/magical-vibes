package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ChargingStrifeknight;
import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.f.FumingEffigy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PillardropWarden;
import com.github.laxika.magicalvibes.cards.s.SpiritMascot;
import com.github.laxika.magicalvibes.cards.s.StoneDocent;
import com.github.laxika.magicalvibes.cards.s.StonebindersFamiliar;
import com.github.laxika.magicalvibes.cards.s.StoneboundMentor;
import com.github.laxika.magicalvibes.cards.s.StoneriseSpirit;
import com.github.laxika.magicalvibes.cards.s.SummonedDromedary;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        BloodAgeMuster.class,
        BloodAgeGeneral.class,
        ChargingStrifeknight.class,
        FumingEffigy.class,
        PillardropWarden.class,
        SpiritMascot.class,
        StoneDocent.class,
        StonebindersFamiliar.class,
        StoneboundMentor.class,
        StoneriseSpirit.class,
        SummonedDromedary.class,
        Disentomb.class,
        GrizzlyBears.class
})
class BloodAgeMusterTest extends BaseCardTest {

    @Test
    void conjuresSpellbookCreatureWithPerpetualBaseStats() {
        Permanent muster = addMuster();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> created = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(muster.getId()))
                .toList();
        assertThat(created).hasSize(1);
        assertThat(created.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(created.getFirst().getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        Permanent muster = addMuster();
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new Disentomb(), new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, first.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(muster.getId()))
                .findFirst()).isPresent();
    }

    private Permanent addMuster() {
        Permanent muster = new Permanent(new BloodAgeMuster());
        gd.playerBattlefields.get(player1.getId()).add(muster);
        return muster;
    }
}
