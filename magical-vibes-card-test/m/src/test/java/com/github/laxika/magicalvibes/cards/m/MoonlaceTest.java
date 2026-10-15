package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.s.Snapback;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Moonlace.class, AshcoatBear.class, ThinkTwice.class, Snapback.class})
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

        harness.castAndResolveInstant(player1, 0, ashcoatBearSpellId);
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

        harness.castAndResolveInstant(player1, 0, targetSpell.getId());

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).isEmpty();

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLUE);
    }

    @Test
    void colorChangeDoesNotFollowPermanentThroughHandAndRecasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new Moonlace(), new Snapback()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveColors(gd, target)).isEmpty();

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertInHand(player1, "Ashcoat Bear");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returnedBear = findPermanent(player1, "Ashcoat Bear");
        assertThat(gqs.getEffectiveColors(gd, returnedBear)).containsExactly(CardColor.GREEN);
    }

    @Test
    void permanentLeavingBeforeResolutionMakesTargetIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new Moonlace(), new Snapback()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Moonlace");
        assertThat(gqs.getEffectiveCardColors(gd, target.getCard())).containsExactly(CardColor.GREEN);
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Moonlace()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
