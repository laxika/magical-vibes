package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AshenmoorGouger;
import com.github.laxika.magicalvibes.cards.c.CeruleanWisps;
import com.github.laxika.magicalvibes.cards.c.Cinderbones;
import com.github.laxika.magicalvibes.cards.g.GoldenglowMoth;
import com.github.laxika.magicalvibes.cards.i.IntimidatorInitiate;
import com.github.laxika.magicalvibes.cards.m.MerrowWavebreakers;
import com.github.laxika.magicalvibes.cards.r.RattleblazeScarecrow;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SootstokeKindler.class, IntimidatorInitiate.class, Cinderbones.class,
        GoldenglowMoth.class, MerrowWavebreakers.class, AshenmoorGouger.class,
        RattleblazeScarecrow.class, SpitefulVisions.class, CeruleanWisps.class})
class SootstokeKindlerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants haste to red creature")
    void resolvingGrantsHasteToRedCreature() {
        addReadyKindler(player1);
        Permanent target = addReadyRedCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Resolving ability grants haste to black creature")
    void resolvingGrantsHasteToBlackCreature() {
        addReadyKindler(player1);
        Permanent target = addReadyBlackCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's red creature")
    void canTargetOpponentRedCreature() {
        addReadyKindler(player1);
        Permanent target = addReadyRedCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is removed at end of turn")
    void hasteRemovedAtEndOfTurn() {
        addReadyKindler(player1);
        Permanent target = addReadyBlackCreature(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target white creature")
    void cannotTargetWhiteCreature() {
        addReadyKindler(player1);
        Permanent target = addReadyWhiteCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot target blue creature")
    void cannotTargetBlueCreature() {
        addReadyKindler(player1);
        Permanent target = addReadyBlueCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Activation taps the source and grants haste only on resolution")
    void tapCostPaidBeforeHasteIsGranted() {
        Permanent kindler = addReadyKindler(player1);
        Permanent target = addReadyRedCreature(player1);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(kindler.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Kindler can activate on the turn it enters because it has haste")
    void newlyEnteredKindlerCanActivate() {
        Permanent kindler = harness.addToBattlefieldAndReturn(player1, new SootstokeKindler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Cinderbones());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(kindler.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.isSummoningSickForTapCost(gd, target, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("A tapped Kindler cannot pay its tap cost again")
    void tappedKindlerCannotActivateAgain() {
        Permanent kindler = addReadyKindler(player1);
        Permanent target = addReadyBlackCreature(player1);
        kindler.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A creature that is both black and red is a legal target")
    void grantsHasteToBlackAndRedCreature() {
        addReadyKindler(player1);
        Permanent target = addCreatureReady(player1, new AshenmoorGouger());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a colorless creature")
    void cannotTargetColorlessCreature() {
        addReadyKindler(player1);
        Permanent target = addCreatureReady(player2, new RattleblazeScarecrow());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Cannot target a black and red noncreature enchantment")
    void cannotTargetBlackAndRedNoncreature() {
        addReadyKindler(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SpitefulVisions());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a");
    }

    @Test
    @DisplayName("Ability still resolves after Kindler leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent kindler = addReadyKindler(player1);
        Permanent target = addReadyBlackCreature(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(kindler);
        gd.playerGraveyards.get(player1.getId()).add(kindler.getCard());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Ability does not grant haste to a target that leaves before resolution")
    void removedTargetDoesNotGainHaste() {
        addReadyKindler(player1);
        Permanent target = addReadyBlackCreature(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target that becomes blue before resolution does not gain haste")
    void targetColorRestrictionRecheckedOnResolution() {
        addReadyKindler(player1);
        Permanent target = addReadyBlackCreature(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.setHand(player2, List.of(new CeruleanWisps()));
        harness.setLibrary(player2, List.of(new Cinderbones()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyKindler(Player player) {
        return addCreatureReady(player, new SootstokeKindler());
    }

    private Permanent addReadyRedCreature(Player player) {
        return addCreatureReady(player, new IntimidatorInitiate());
    }

    private Permanent addReadyBlackCreature(Player player) {
        return addCreatureReady(player, new Cinderbones());
    }

    private Permanent addReadyWhiteCreature(Player player) {
        return addCreatureReady(player, new GoldenglowMoth());
    }

    private Permanent addReadyBlueCreature(Player player) {
        return addCreatureReady(player, new MerrowWavebreakers());
    }
}
