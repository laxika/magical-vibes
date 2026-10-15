package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IshkanahGrafwidow.class, GrizzlyBears.class, Forest.class, Shock.class,
        Millstone.class, GiantSpider.class})
class IshkanahGrafwidowTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, the ETB creates three 1/2 green Spider tokens with reach")
    void deliriumCreatesSpiderTokens() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.castFromHand(player1, new IshkanahGrafwidow(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIDER);
            assertThat(token.getCard().getKeywords()).contains(Keyword.REACH);
        });
    }

    @Test
    @DisplayName("Without delirium, the ETB creates no Spider tokens")
    void withoutDeliriumCreatesNoSpiderTokens() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.castFromHand(player1, new IshkanahGrafwidow(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The activated ability makes an opponent lose 1 life for each Spider you control")
    void activatedAbilityScalesWithControlledSpiders() {
        harness.addToBattlefield(player1, new IshkanahGrafwidow());
        harness.addToBattlefield(player1, new GiantSpider());
        harness.addToBattlefield(player2, new GiantSpider());
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The activated ability cannot target its controller")
    void activatedAbilityCannotTargetController() {
        harness.addToBattlefield(player1, new IshkanahGrafwidow());
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Delirium must still be satisfied when the ETB ability resolves")
    void losingDeliriumBeforeResolutionCreatesNoTokens() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.castFromHand(player1, new IshkanahGrafwidow(), "{4}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Gaining delirium after entry does not create an ETB trigger")
    void gainingDeliriumAfterEntryDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.castFromHand(player1, new IshkanahGrafwidow(), "{4}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Four cards of only three types do not satisfy delirium")
    void duplicateCardTypesDoNotSatisfyDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GiantSpider(), new Forest(), new Shock()));
        harness.castFromHand(player1, new IshkanahGrafwidow(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Only the controller's graveyard contributes to delirium")
    void opponentGraveyardDoesNotSatisfyDelirium() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new Millstone()));
        harness.castFromHand(player1, new IshkanahGrafwidow(), "{4}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The activated ability counts Spider tokens as well as Ishkanah")
    void activatedAbilityCountsCreatedTokens() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.castFromHand(player1, new IshkanahGrafwidow(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        addManaForAbility();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The activated ability counts Spiders at resolution even after Ishkanah leaves")
    void activatedAbilityCountsSpidersAfterSourceLeaves() {
        Permanent ishkanah = harness.addToBattlefieldAndReturn(player1, new IshkanahGrafwidow());
        harness.addToBattlefield(player1, new GiantSpider());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ishkanah));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The activated ability causes no life loss when no Spiders remain")
    void activatedAbilityWithNoRemainingSpiders() {
        Permanent ishkanah = harness.addToBattlefieldAndReturn(player1, new IshkanahGrafwidow());
        addManaForAbility();
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, ishkanah));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
