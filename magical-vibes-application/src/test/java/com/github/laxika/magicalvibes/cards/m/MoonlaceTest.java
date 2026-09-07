package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Moonlace.class, GrizzlyBears.class})
class MoonlaceTest extends BaseCardTest {

    @Test
    void permanentBecomesColorless() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Moonlace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
    }

    @Test
    void colorlessSettingPersistsPastEndOfTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Moonlace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
    }

    @Test
    void spellTargetCarriesColorlessSettingToPermanent() {
        harness.setHand(player1, List.of(new Moonlace(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 1);
        UUID bearsSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, bearsSpellId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }
}
