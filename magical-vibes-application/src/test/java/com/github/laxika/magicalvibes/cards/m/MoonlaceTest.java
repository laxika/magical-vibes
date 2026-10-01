package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Moonlace.class, AshcoatBear.class, ThinkTwice.class})
class MoonlaceTest extends BaseCardTest {

    @Test
    void permanentBecomesColorless() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Moonlace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
    }

    @Test
    void colorlessSettingPersistsPastEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Moonlace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();
    }

    @Test
    void spellTargetCarriesColorlessSettingToPermanent() {
        harness.setHand(player1, List.of(new Moonlace(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 1);
        UUID ashcoatBearSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, ashcoatBearSpellId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ashcoatBear = findPermanent(player1, "Ashcoat Bear");
        assertThat(gqs.getEffectiveColors(gd, ashcoatBear)).isEmpty();
    }

    @Test
    void nonpermanentSpellBecomesColorlessOnlyWhileOnStack() {
        harness.setHand(player1, List.of(new Moonlace(), new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 1);
        Card targetSpell = gd.stack.getFirst().getCard();

        harness.castInstant(player1, 0, targetSpell.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).isEmpty();

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLUE);
    }
}
