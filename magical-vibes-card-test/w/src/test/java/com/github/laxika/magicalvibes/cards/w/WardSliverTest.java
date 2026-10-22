package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.p.PlatedSliver;
import com.github.laxika.magicalvibes.cards.r.Reanimate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WardSliver.class, PlatedSliver.class, FugitiveWizard.class, Reanimate.class})
class WardSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen color protection applies to all Slivers")
    void grantsProtectionFromChosenColorToAllSlivers() {
        Permanent ownSliver = harness.addToBattlefieldAndReturn(player1, new PlatedSliver());
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new PlatedSliver());
        Permanent nonSliver = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        harness.castFromHand(player1, new WardSliver(), "{4}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent wardSliver = findPermanent(player1, "Ward Sliver");
        assertThat(gqs.hasProtectionFrom(gd, wardSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingSliver, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nonSliver, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownSliver, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Protection applies to later Slivers and ends when Ward Sliver leaves")
    void protectionTracksLaterSliversAndSourceLeaving() {
        harness.castFromHand(player1, new WardSliver(), "{4}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        Permanent laterSliver = harness.enterBattlefieldAndReturn(player2, new PlatedSliver());
        assertThat(gqs.hasProtectionFrom(gd, laterSliver, CardColor.RED)).isTrue();

        Permanent wardSliver = findPermanent(player1, "Ward Sliver");
        gd.playerBattlefields.get(player1.getId()).remove(wardSliver);

        assertThat(gqs.hasProtectionFrom(gd, laterSliver, CardColor.RED)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("A Ward Sliver entering without being cast can choose any color")
    void choosesAnyColorWhenEnteringWithoutBeingCast(CardColor chosenColor) {
        Permanent opposingSliver = harness.addToBattlefieldAndReturn(player2, new PlatedSliver());
        reanimateWardSliver(player1);
        harness.handleListChoice(player1, chosenColor.name());
        Permanent wardSliver = findPermanent(player1, "Ward Sliver");

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, wardSliver, color)).isEqualTo(color == chosenColor);
            assertThat(gqs.hasProtectionFrom(gd, opposingSliver, color)).isEqualTo(color == chosenColor);
        }
        assertThat(gd.stack).isEmpty();
    }

    /** Returns a Ward Sliver from the caster's graveyard to the battlefield with Reanimate, so it enters as a non-cast permanent. */
    private void reanimateWardSliver(Player caster) {
        Card wardSliver = new WardSliver();
        harness.setGraveyard(caster, List.of(wardSliver));
        harness.setHand(caster, List.of(new Reanimate()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(caster, 0, wardSliver.getId());
    }

    @Test
    @DisplayName("Multiple Ward Slivers grant independent chosen colors to both players' Slivers")
    void multipleWardSliversKeepIndependentChoices() {
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new PlatedSliver());
        reanimateWardSliver(player1);
        harness.handleListChoice(player1, "RED");
        Permanent redWard = findPermanent(player1, "Ward Sliver");
        reanimateWardSliver(player2);
        harness.handleListChoice(player2, "BLUE");
        Permanent blueWard = findPermanent(player2, "Ward Sliver");

        for (Permanent permanent : java.util.List.of(sliver, redWard, blueWard)) {
            assertThat(gqs.hasProtectionFrom(gd, permanent, CardColor.RED)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, permanent, CardColor.BLUE)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, permanent, CardColor.BLACK)).isFalse();
        }

        gd.playerBattlefields.get(player1.getId()).remove(redWard);

        for (Permanent permanent : java.util.List.of(sliver, blueWard)) {
            assertThat(gqs.hasProtectionFrom(gd, permanent, CardColor.RED)).isFalse();
            assertThat(gqs.hasProtectionFrom(gd, permanent, CardColor.BLUE)).isTrue();
        }
    }
}
