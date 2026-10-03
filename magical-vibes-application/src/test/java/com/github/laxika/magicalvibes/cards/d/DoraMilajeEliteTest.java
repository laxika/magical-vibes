package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EiganjoCastle;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DoraMilajeElite.class, EiganjoCastle.class, Forest.class, GrizzlyBears.class})
class DoraMilajeEliteTest extends BaseCardTest {

    @Test
    void createsATappedIndestructibleVibraniumWhenOpponentControlsMoreLands() {
        harness.addToBattlefield(player2, new Forest());
        castDora();

        Permanent vibranium = findPermanent(player1, "Vibranium");
        assertThat(vibranium.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, vibranium, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void vibraniumAddsRestrictedColorlessMana() {
        harness.addToBattlefield(player2, new Forest());
        castDora();

        Permanent vibranium = findPermanent(player1, "Vibranium");
        vibranium.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);

        assertThat(vibranium.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void doesNotCreateVibraniumWhenLandCountsAreEqual() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castDora();

        assertThat(findPermanents(player1, "Vibranium")).isEmpty();
    }

    @Test
    void sacrificingDoraProtectsOnlyOwnLegendaryPermanents() {
        Permanent dora = addDoraReady();
        Permanent ownLegendary = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());
        Permanent ownNonLegendary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLegendary = harness.addToBattlefieldAndReturn(player2, new EiganjoCastle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dora);
        assertThat(gqs.hasKeyword(gd, ownLegendary, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownNonLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void indestructibleWearsOffAtEndOfTurn() {
        addDoraReady();
        Permanent ownLegendary = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ownLegendary, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void createdTokenHasVibraniumSubtype() {
        harness.addToBattlefield(player2, new Forest());
        castDora();

        assertThat(findPermanent(player1, "Vibranium").getCard().getSubtypes())
                .extracting(CardSubtype::getDisplayName)
                .contains("Vibranium");
    }

    @Test
    void landConditionIsCheckedAgainWhenTriggerResolves() {
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new DoraMilajeElite(), "{1}{W}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vibranium")).isEmpty();
    }

    @Test
    void vibraniumManaCannotPayForANonartifactSpell() {
        harness.addToBattlefield(player2, new Forest());
        castDora();
        Permanent vibranium = findPermanent(player1, "Vibranium");
        vibranium.untap();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vibranium), 0, null, null);
        harness.setHand(player1, List.of(new DoraMilajeElite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }

    @Test
    void equalLandCountsDoNotPutAnAbilityOnTheStack() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.castFromHand(player1, new DoraMilajeElite(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Vibranium")).isEmpty();
    }

    @Test
    void sacrificeIsPaidImmediatelyEvenWhileSummoningSickAndTapped() {
        Permanent dora = harness.addToBattlefieldAndReturn(player1, new DoraMilajeElite());
        dora.setSummoningSick(true);
        dora.tap();
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dora);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dora.getCard());
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void legendaryPermanentEnteringBeforeResolutionIsProtected() {
        addDoraReady();
        harness.activateAbility(player1, 0, null, null);
        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void legendaryPermanentEnteringAfterResolutionIsNotProtected() {
        addDoraReady();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent legendary = harness.addToBattlefieldAndReturn(player1, new EiganjoCastle());

        assertThat(gqs.hasKeyword(gd, legendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void castDora() {
        harness.castFromHand(player1, new DoraMilajeElite(), "{1}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addDoraReady() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new DoraMilajeElite());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
