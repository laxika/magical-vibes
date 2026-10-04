package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HumbleDefector;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.v.VeyranVoiceOfDuality;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeldonOfTheThirdPath.class, GrizzlyBears.class, Plains.class, Clone.class,
        HumbleDefector.class, VeyranVoiceOfDuality.class})
class FeldonOfTheThirdPathTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an artifact creature copy with haste and sacrifices it at the next end step")
    void createsHastyArtifactCopyAndSacrificesItAtEndStep() {
        Permanent feldon = addCreatureReady(player1, new FeldonOfTheThirdPath());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        addFeldonMana();

        int feldonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(feldon);
        harness.activateAbilityWithGraveyardTargets(player1, feldonIndex, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(token.getId()));
    }

    @Test
    @DisplayName("Rejects a noncreature graveyard target")
    void rejectsNoncreatureTarget() {
        Permanent feldon = addCreatureReady(player1, new FeldonOfTheThirdPath());
        Card plains = new Plains();
        harness.setGraveyard(player1, List.of(plains));
        addFeldonMana();

        int feldonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(feldon);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, feldonIndex, 0, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    @Test
    void leavesCopiedCardInGraveyard() {
        Card bears = new GrizzlyBears();
        createCopy(bears);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    void preservesAllColorsOfCopiedCreature() {
        Permanent token = createCopy(new VeyranVoiceOfDuality());

        assertThat(gqs.getEffectiveColors(gd, token))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.BLUE);
    }

    @Test
    void rejectsOpponentsGraveyard() {
        addCreatureReady(player1, new FeldonOfTheThirdPath());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        addFeldonMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void createsNoTokenWhenTargetLeavesGraveyardBeforeResolution() {
        Permanent feldon = addCreatureReady(player1, new FeldonOfTheThirdPath());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        addFeldonMana();
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(feldon);
        assertThat(feldon.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new FeldonOfTheThirdPath());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        addFeldonMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new FeldonOfTheThirdPath());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyOfTokenIsArtifactButDoesNotInheritGrantedHasteOrSacrifice() {
        Permanent token = createCopy(new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof Clone)
                .findFirst().orElseThrow();
        assertThat(clone.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(clone.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(clone).doesNotContain(token);
    }

    @Test
    void cannotSacrificeTokenAfterOpponentGainsControl() {
        harness.forceActivePlayer(player1);
        Permanent token = createCopy(new HumbleDefector());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(token);
    }

    @Test
    void tokenCreatedDuringEndStepSurvivesUntilFollowingEndStep() {
        harness.passUntil(TurnStep.END_STEP);
        Permanent token = createCopy(new GrizzlyBears());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    private Permanent createCopy(Card target) {
        Permanent feldon = addCreatureReady(player1, new FeldonOfTheThirdPath());
        harness.setGraveyard(player1, List.of(target));
        addFeldonMana();
        int feldonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(feldon);
        harness.activateAbilityWithGraveyardTargets(player1, feldonIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }

    private void addFeldonMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
