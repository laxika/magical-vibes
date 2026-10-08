package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WilyBandar.class, TidyConclusion.class, ChandrasPyrohelix.class})
class WilyBandarTest extends BaseCardTest {

    @Test
    void activatedAbilityGrantsIndestructibleUntilEndOfTurn() {
        Permanent bandar = addCreatureReady(player1, new WilyBandar());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandar, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void grantedIndestructibleResetsAtEndOfTurn() {
        Permanent bandar = addCreatureReady(player1, new WilyBandar());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bandar, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandar, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent bandar = harness.addToBattlefieldAndReturn(player1, new WilyBandar());
        bandar.setSummoningSick(true);
        bandar.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gqs.hasKeyword(gd, bandar, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bandar, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(bandar.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        harness.addToBattlefield(player1, new WilyBandar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyTwoMana() {
        harness.addToBattlefield(player1, new WilyBandar());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activationInResponsePreventsDestruction() {
        Permanent bandar = harness.addToBattlefieldAndReturn(player1, new WilyBandar());
        harness.setHand(player1, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstant(player1, 0, bandar.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wily Bandar");
        harness.assertNotInGraveyard(player1, "Wily Bandar");
    }

    @Test
    void indestructibleSurvivesLethalDamageAndCleanup() {
        Permanent bandar = harness.addToBattlefieldAndReturn(player1, new WilyBandar());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new ChandrasPyrohelix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, Map.of(bandar.getId(), 2));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wily Bandar");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wily Bandar");
        assertThat(gqs.hasKeyword(gd, bandar, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
