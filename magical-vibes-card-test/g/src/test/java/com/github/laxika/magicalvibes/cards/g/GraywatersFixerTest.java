package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GraywatersFixer.class, GrizzlyBears.class, DressDown.class})
@DisplayName("Graywater's Fixer")
class GraywatersFixerTest extends BaseCardTest {

    @ParameterizedTest
    @EnumSource(value = CardSubtype.class, names = {"ASSASSIN", "MERCENARY", "PIRATE", "ROGUE", "WARLOCK"})
    @DisplayName("Grants encore to outlaw creature cards in your graveyard")
    void grantsEncoreToOutlawCreatureCards(CardSubtype outlawSubtype) {
        Card outlaw = new GrizzlyBears();
        outlaw.setSubtypes(List.of(outlawSubtype));
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(outlaw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isAttacking()).isFalse();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isMustAttackThisTurn()).isTrue();
        assertThat(token.getMustAttackTargetId()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant encore to non-outlaw creature cards")
    void doesNotGrantEncoreToNonOutlawCreatureCards() {
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesOutlawAsCostAndCreatesOneHastyCopyForOpponent() {
        GraywatersFixer outlaw = new GraywatersFixer();
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(outlaw));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(outlaw.getId())).isNotNull();
        assertThat(findPermanents(player1, "Graywater's Fixer")).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Graywater's Fixer").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.hasKeyword(gd, tokens.getFirst(), Keyword.HASTE)).isTrue();
        assertThat(tokens.getFirst().isAttacking()).isFalse();
    }

    @Test
    void requiresManaEqualToOutlawsManaValue() {
        GraywatersFixer outlaw = new GraywatersFixer();
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(outlaw));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(outlaw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotGrantEncoreToOpponentsGraveyard() {
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player2, List.of(new GraywatersFixer()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "BEGINNING_OF_COMBAT", "END_STEP"})
    void cannotActivateEncoreOutsideMainPhase(TurnStep step) {
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(new GraywatersFixer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Graywater's Fixer");
    }

    @Test
    void resolvesAfterFixerLeavesAndSacrificesCopyAtNextEndStep() {
        Permanent fixer = harness.addToBattlefieldAndReturn(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(new GraywatersFixer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateGraveyardAbility(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(fixer);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Graywater's Fixer");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isAttacking()).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Graywater's Fixer");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Graywater's Fixer");
    }

    @Test
    void stopsGrantingEncoreWhenFixerLosesItsAbilities() {
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.addToBattlefield(player2, new DressDown());
        harness.setGraveyard(player1, List.of(new GraywatersFixer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Graywater's Fixer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotSacrificeEncoreTokenControlledByOpponent() {
        harness.addToBattlefield(player1, new GraywatersFixer());
        harness.setGraveyard(player1, List.of(new GraywatersFixer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent token = findPermanents(player1, "Graywater's Fixer").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(token);
        gd.playerBattlefields.get(player2.getId()).add(token);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }
}
